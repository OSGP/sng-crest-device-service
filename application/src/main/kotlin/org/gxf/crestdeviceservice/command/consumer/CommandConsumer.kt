// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.consumer

import com.alliander.sng.Command as ExternalCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.mapper.CommandMapper
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class CommandConsumer(
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService,
    private val pskService: PskService
) {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(id = "command", idIsGroup = false, topics = ["\${kafka.consumers.command.topic}"])
    fun handleIncomingCommand(externalCommand: ExternalCommand) {
        logger.info {
            "Received command ${externalCommand.command} for device: ${externalCommand.deviceId}, with correlation id: ${externalCommand.correlationId}"
        }
        try {
            val pendingCommand =
                CommandMapper.externalCommandToCommandEntity(externalCommand, Command.CommandStatus.PENDING)

            commandService.validate(pendingCommand)
            commandFeedbackService.sendReceivedFeedback(pendingCommand)
            commandService.cancelOlderCommandIfNecessary(pendingCommand)

            if (commandService.isPskCommand(pendingCommand)) {
                pskService.generateNewReadyKeyForDevice(externalCommand.deviceId)
            }

            commandService.saveCommand(pendingCommand)
        } catch (exception: CommandValidationException) {
            val reason = exception.message ?: ""
            logger.warn {
                "Command ${externalCommand.command} for device ${externalCommand.deviceId} is rejected. Reason: $reason"
            }
            commandFeedbackService.sendRejectionFeedback(reason, externalCommand)
        }
    }
}
