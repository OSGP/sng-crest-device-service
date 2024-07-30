// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import com.alliander.sng.Command as ExternalCommand
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class CommandConsumer(
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService
) {

    private val logger = KotlinLogging.logger {}

    @KafkaListener(
        id = "pre-shared-key",
        idIsGroup = false,
        topics = ["\${kafka.consumers.pre-shared-key.topic}"])
    fun handleIncomingCommand(command: ExternalCommand) {
        logger.info { "Received command for device: ${command.deviceId}, with correlation id: ${command.correlationId}" }

        val shouldBeRejected = commandService.shouldBeRejected(command)

        if(shouldBeRejected.isPresent) {
            logger.info { "Rejecting command for device id: ${command.deviceId}, with reason: ${shouldBeRejected.get()}" }
            commandFeedbackService.rejectCommand(command, shouldBeRejected.get())
            return
        }

        commandService.saveCommand(command)

    }
}
