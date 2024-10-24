// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.generator.CommandGenerator
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

/** Creates downlinks to be sent to a device after it makes contact. */
@Service
class DownlinkService(
    private val pskService: PskService,
    private val commandService: CommandService,
    private val messageProperties: MessageProperties,
    commandGenerators: List<CommandGenerator>
) {

    private val logger = KotlinLogging.logger {}

    private val commandGenerators = commandGenerators.associateBy { it.getSupportedCommand() }

    fun createDownlink() = Downlink(messageProperties.maxBytes)

    @Transactional
    @Throws(NoExistingPskException::class)
    fun getDownlinkForDevice(deviceId: String, downlink: Downlink): String {
        logger.info { "Gathering pending commands for device $deviceId" }
        val pendingCommands = commandService.getAllPendingCommandsForDevice(deviceId)
        val commandsToSend = getCommandsToSend(pendingCommands)
        if (commandsToSend.isNotEmpty()) {
            downlink.fillFromCommands(commandsToSend)
        }
        return downlink.getDownlink()
    }

    private fun getCommandsToSend(pendingCommands: List<Command>) =
        pendingCommands.filter { command -> commandCanBeSent(command) }

    private fun commandCanBeSent(command: Command) =
        when (command.type) {
            Command.CommandType.PSK_SET -> pskService.readyForPskSetCommand(command.deviceId)
            else -> true
        }

    private fun Downlink.fillFromCommands(pendingCommands: List<Command>) {
        logger.info { "Device has pending commands of types: ${getPrintableCommandTypes(pendingCommands)}." }

        val commandsToSend = pendingCommands.filter { command -> addIfPossible(getDownlinkPerCommand(command)) }
        logger.info { "Commands that will be sent: ${getPrintableCommandTypes(commandsToSend)}." }

        commandsToSend.forEach { command -> setCommandInProgress(command) }

        logger.debug { "Downlink that will be sent: ${getDownlink()}" }
    }

    private fun getPrintableCommandTypes(commands: List<Command>) =
        commands.joinToString(", ") { command -> command.type.toString() }

    private fun setCommandInProgress(command: Command) {
        if (command.type == Command.CommandType.PSK_SET) {
            val deviceId = command.deviceId
            logger.info { "Device $deviceId needs key change" }
            pskService.setPskToPendingForDevice(deviceId)
        }
        commandService.saveCommand(command.start())
    }

    private fun getDownlinkPerCommand(command: Command) =
        commandGenerators[command.type]?.generateCommandString(command) ?: command.type.downlink
}
