// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.exception.NoCommandResultHandlerForCommandTypeException
import org.gxf.crestdeviceservice.command.feedbackgenerator.CommandFeedbackGenerator
import org.gxf.crestdeviceservice.command.resulthandler.CommandResultHandler
import org.springframework.stereotype.Service

@Service
class CommandResultService(
    private val commandService: CommandService,
    private val commandResultHandlersByType: Map<CommandType, CommandResultHandler>,
    private val commandFeedbackGenerators: List<CommandFeedbackGenerator>,
) {
    private val logger = KotlinLogging.logger {}

    fun handleMessage(deviceId: String, message: JsonNode) {
        val commandsInProgress = commandService.getAllCommandsInProgressForDevice(deviceId)

        commandsInProgress.forEach { checkResult(it, message) }
    }

    private fun checkResult(command: Command, message: JsonNode) {
        logger.debug { "Checking result for pending command of type ${command.type} for device ${command.deviceId}" }

        val resultHandler =
            commandResultHandlersByType[command.type]
                ?: throw NoCommandResultHandlerForCommandTypeException(
                    "No command result handler for command type ${command.type}"
                )
        handleResult(command, resultHandler, message)
    }

    private fun handleResult(command: Command, resultHandler: CommandResultHandler, message: JsonNode) {
        val feedbackGenerator = commandFeedbackGenerators.firstOrNull { it.supportedCommandType == command.type }

        when {
            resultHandler.hasSucceeded(command, message) ->
                resultHandler.handleSuccess(command, message, feedbackGenerator)
            resultHandler.hasFailed(command, message) -> resultHandler.handleFailure(command, message)
            else -> resultHandler.handleStillInProgress(command)
        }
    }
}
