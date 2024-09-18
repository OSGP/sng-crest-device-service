// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service

/**
 * @param pskService
 * @param commandService
 * @param messageProperties
 */
@Service
class DownlinkService(
    private val pskService: PskService,
    private val commandService: CommandService,
    private val messageProperties: MessageProperties
) {
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
        logger.info {
            "Device $deviceId has pending commands of types: ${getPrintableCommandTypes(pendingCommands)}."
        }

        val downlink = Downlink(messageProperties.maxBytes)

        val commandsToSend =
            pendingCommands.filter { command ->
                downlink.addIfItFits(getDownlinkPerCommand(command))
            }

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

    private fun getDownlinkPerCommand(command: Command): String {
        if (command.type == Command.CommandType.PSK) {
            val newKey = getCurrentReadyPsk(command)

            return createPskCommand(newKey)
        }
        if (command.type == Command.CommandType.PSK_SET) {
            val newKey = getCurrentReadyPsk(command)
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and" +
                        " status ${newKey.status}"
            }
            return createPskSetCommand(newKey)
        }

        return command.type.downlink
    }

    private fun getCurrentReadyPsk(command: Command) =
        pskService.getCurrentReadyPsk(command.deviceId)
            ?: throw NoExistingPskException("There is no new key ready to be set")

    fun createPskCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}$newKey")
        return "PSK:$newKey:$hash"
    }

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}$newKey")
        return "PSK:$newKey:$hash:SET"
    }
    companion object {
        private const val RESPONSE_SUCCESS = "0"
    }
}
