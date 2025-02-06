// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.feedbackgenerator.CommandFeedbackGenerator
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.getMessageFromCode

abstract class CommandResultHandler(
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService,
) {
    private val logger = KotlinLogging.logger {}

    abstract val supportedCommandType: CommandType

    abstract fun hasSucceeded(command: Command, message: JsonNode): Boolean

    abstract fun hasFailed(command: Command, message: JsonNode): Boolean

    fun handleSuccess(command: Command, message: JsonNode, feedbackGenerator: CommandFeedbackGenerator? = null) {
        logger.info { "Command ${command.type} succeeded for device with id ${command.deviceId}." }

        handleCommandSpecificSuccess(command, message)

        logger.debug { "Saving command and sending feedback to Maki." }
        val successfulCommand = commandService.saveCommand(command.finish())

        sendSuccessFeedback(feedbackGenerator, message, successfulCommand)
    }

    private fun sendSuccessFeedback(
        feedbackGenerator: CommandFeedbackGenerator?,
        message: JsonNode,
        successfulCommand: Command,
    ) {
        if (feedbackGenerator == null) {
            commandFeedbackService.sendSuccessFeedback(successfulCommand)
        } else {
            val feedback = feedbackGenerator.generateFeedback(message)
            commandFeedbackService.sendSuccessFeedback(successfulCommand, feedback)
        }
    }

    /** Override this method when custom success actions are needed. */
    open fun handleCommandSpecificSuccess(command: Command, message: JsonNode) {
        logger.debug {
            "Command ${command.type} for device with id ${command.deviceId} does not require specific success handling."
        }
    }

    fun handleFailure(command: Command, message: JsonNode) {
        logger.info { "Command ${command.type} failed for device with id ${command.deviceId}." }

        handleCommandSpecificFailure(command, message)

        val failedCommand = commandService.saveCommand(command.fail())
        val errorMessages = message.urcs().joinToString(". ") { urc -> getMessageFromCode(urc) }
        commandFeedbackService.sendErrorFeedback(failedCommand, "Command failed. Error(s): $errorMessages.")
    }

    /** Override this method when command specific failure actions are needed */
    open fun handleCommandSpecificFailure(command: Command, message: JsonNode) {
        logger.debug {
            "Command ${command.type} for device with id ${command.deviceId} does not require specific failure handling."
        }
    }

    fun handleStillInProgress(command: Command) {
        logger.info { "Command ${command.type} still in progress for device with id ${command.deviceId}." }
    }

    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"

        fun JsonNode.urcs(): List<String> = this[URC_FIELD].filter { it.isTextual }.map { it.asText() }

        fun JsonNode.downlinks(): List<String> =
            this[URC_FIELD].first { it.isObject }[DL_FIELD].asText().replace("!", "").split(";")
    }
}
