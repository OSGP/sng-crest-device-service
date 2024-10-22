// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(
    private val deviceService: DeviceService,
    private val pskService: PskService,
    private val commandService: CommandService,
    private val messageProperties: MessageProperties
) {
    companion object {
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    @Throws(NoExistingPskException::class)
    fun getDownlinkForDevice(deviceId: String, body: JsonNode): String {
        val pendingCommands = commandService.getAllPendingCommandsForDevice(deviceId)
        val commandsToSend = getCommandsToSend(pendingCommands)
        if (commandsToSend.isNotEmpty()) {
            return getDownlinkFromCommands(deviceId, commandsToSend)
        }
        return RESPONSE_SUCCESS
    }

    private fun getCommandsToSend(pendingCommands: List<Command>) =
        pendingCommands.filter { command -> commandCanBeSent(command) }

    private fun commandCanBeSent(command: Command) =
        when (command.type) {
            Command.CommandType.PSK_SET -> pskService.readyForPskSetCommand(command.deviceId)
            else -> true
        }

    private fun getDownlinkFromCommands(deviceId: String, pendingCommands: List<Command>): String {
        logger.info { "Device $deviceId has pending commands of types: ${getPrintableCommandTypes(pendingCommands)}." }

        val downlink = Downlink(messageProperties.maxBytes)

        val commandsToSend = pendingCommands.filter { command -> downlink.addIfItFits(getDownlinkPerCommand(command)) }

        logger.info { "Commands that will be sent: ${getPrintableCommandTypes(commandsToSend)}." }
        commandsToSend.forEach { command -> setCommandInProgress(command) }

        val completeDownlink = downlink.downlink
        logger.debug { "Downlink that will be sent: $completeDownlink" }
        return completeDownlink
    }

    private fun getPrintableCommandTypes(commands: List<Command>) =
        commands.joinToString(", ") { command -> command.type.toString() }

    private fun setCommandInProgress(command: Command) {
        if (command.type == Command.CommandType.PSK_SET) {
            val deviceId = command.deviceId
            logger.info { "Device $deviceId needs key change" }
            pskService.setPskToPendingForDevice(deviceId)
        }
        commandService.saveCommandWithNewStatus(command, Command.CommandStatus.IN_PROGRESS)
    }

    private fun getDownlinkPerCommand(command: Command) =
        when (command.type) {
            Command.CommandType.PSK -> {
                val device = deviceService.getDevice(command.deviceId)
                val newKey = getCurrentReadyPsk(command)

                createPskCommand(device, newKey)
            }
            Command.CommandType.PSK_SET -> {
                val device = deviceService.getDevice(command.deviceId)
                val newKey = getCurrentReadyPsk(command)

                logger.debug {
                    "Create PSK set command for key for device ${device.id} with revision ${newKey.revision} and status ${newKey.status}"
                }

                createPskSetCommand(device, newKey)
            }
            else -> command.type.downlink
        }

    private fun getCurrentReadyPsk(command: Command) =
        pskService.getCurrentReadyPsk(command.deviceId)
            ?: throw NoExistingPskException("There is no new key ready to be set")

    fun createPskCommand(device: Device, newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${device.secret}${newKey}")
        return "PSK:${newKey}:${hash}"
    }

    fun createPskSetCommand(device: Device, newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${device.secret}${newKey}")
        return "PSK:${newKey}:${hash}:SET"
    }
}
