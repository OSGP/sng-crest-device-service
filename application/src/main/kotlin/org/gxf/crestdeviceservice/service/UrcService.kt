// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.CommandStatus as ExternalCommandStatus
import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.exception.NoMatchingCommandException
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.getMessageFromCode
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class UrcService(
    private val pskService: PskService,
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService
) {
    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
    }

    private val logger = KotlinLogging.logger {}

    fun interpretURCsInMessage(deviceId: String, body: JsonNode) {
        val urcs = getUrcsFromMessage(body)
        if (urcs.isEmpty()) {
            logger.debug { "Received message without urcs" }
        } else {
            logger.debug { "Received message with urcs ${urcs.joinToString(", ")}" }
        }

        val downlinks = getDownlinksFromMessage(body).filter { downlink -> downlink != "0" && downlink.isNotBlank() }
        downlinks.forEach { downlink -> handleDownlinkFromMessage(deviceId, downlink, urcs) }
    }

    private fun getUrcsFromMessage(body: JsonNode) = body[URC_FIELD].filter { it.isTextual }.map { it.asText() }

    private fun getDownlinksFromMessage(body: JsonNode) =
        body[URC_FIELD].first { it.isObject }[DL_FIELD].asText().split(";")

    private fun handleDownlinkFromMessage(deviceId: String, downlink: String, urcs: List<String>) {
        val command = getCommandThatDownlinkIsAbout(deviceId, downlink)

        if (command != null) {
            handleUrcsForCommand(urcs, command, downlink)
        } else {
            throw NoMatchingCommandException(
                "Message received with downlink: $downlink, but there is no matching command in progress in the database.")
        }
    }

    private fun getCommandThatDownlinkIsAbout(deviceId: String, downlink: String): Command? {
        val commandsInProgress = commandService.getAllCommandsInProgressForDevice(deviceId)
        return try {
            commandsInProgress.first { command -> downlinkConcernsCommandType(downlink, command.type) }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    private fun downlinkConcernsCommandType(downlink: String, commandType: Command.CommandType): Boolean {
        return if (commandType == Command.CommandType.PSK) {
            // do not treat PSK_SET downlink as PSK command
            !downlinkMatchesForCommandType(downlink, Command.CommandType.PSK_SET)
        } else {
            downlinkMatchesForCommandType(downlink, commandType)
        }
    }

    private fun downlinkMatchesForCommandType(downlink: String, commandType: Command.CommandType): Boolean {
        val parts = commandType.downlink.split(":")
        return parts.all { part -> downlink.contains(part) }
    }

    private fun handleUrcsForCommand(urcs: List<String>, command: Command, downlink: String) {
        when {
            urcsContainErrorsForCommand(urcs, command) -> handleCommandErrors(command, urcs)
            urcsContainSuccessesForCommand(urcs, command) -> handleCommandSuccesses(command)
            else ->
                logger.warn {
                    "No urcs received for command '${command.type}' that was sent in downlink: $downlink. Urcs received: ${
                        urcs.joinToString()
                    }."
                }
        }
    }

    private fun urcsContainErrorsForCommand(urcs: List<String>, command: Command) =
        command.type.urcsError.any { errorUrc -> urcs.contains(errorUrc) }

    private fun urcsContainSuccessesForCommand(urcs: List<String>, command: Command) =
        command.type.urcsSuccess.any { successUrc -> urcs.contains(successUrc) }

    private fun handleCommandErrors(command: Command, urcs: List<String>) {
        if (command.type == Command.CommandType.PSK_SET) {
            handlePskErrors(command.deviceId)
        }
        val errorMessages = urcs.joinToString(". ") { urc -> getMessageFromCode(urc) }

        logger.error {
            "Command ${command.type} failed for device with id ${command.deviceId}. Error(s): $errorMessages."
        }

        val failedCommand = commandService.saveCommandWithNewStatus(command, CommandStatus.ERROR)
        val commandFeedback =
            CommandFeedbackMapper.commandEntityToCommandFeedback(
                failedCommand, ExternalCommandStatus.Error, "Command failed. Error(s): $errorMessages.")
        commandFeedbackService.sendFeedback(commandFeedback)
    }

    private fun handlePskErrors(deviceId: String) {
        if (!pskService.isPendingPskPresent(deviceId)) {
            throw NoExistingPskException("Failure URC received, but no pending key present to set as invalid")
        }
        pskService.setPendingKeyAsInvalid(deviceId)
    }

    private fun handleCommandSuccesses(command: Command) {
        if (command.type == Command.CommandType.PSK_SET) {
            handlePskSetSuccess(command)
        }
        logger.info {
            "Command ${command.type} for device ${command.deviceId} handled successfully. Saving command and sending feedback to Maki."
        }

        val successfulCommand = commandService.saveCommandWithNewStatus(command, CommandStatus.SUCCESSFUL)
        val commandFeedback =
            CommandFeedbackMapper.commandEntityToCommandFeedback(
                successfulCommand, ExternalCommandStatus.Successful, "Command handled successfully")
        commandFeedbackService.sendFeedback(commandFeedback)
    }

    private fun handlePskSetSuccess(command: Command) {
        val deviceId = command.deviceId
        if (!pskService.isPendingPskPresent(deviceId)) {
            throw NoExistingPskException("Success URC received, but no pending key present to set as active")
        }
        logger.info { "PSK set successfully, changing active key" }
        pskService.changeActiveKey(deviceId)
    }
}
