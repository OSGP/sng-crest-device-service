// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus.Cancelled
import com.alliander.sng.CommandStatus.Error
import com.alliander.sng.CommandStatus.Progress
import com.alliander.sng.CommandStatus.Received
import com.alliander.sng.CommandStatus.Rejected
import com.alliander.sng.CommandStatus.Successful
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper.commandEntityToCommandFeedback
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper.externalCommandToCommandFeedback
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class CommandFeedbackService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    kafkaProducerProperties: KafkaProducerProperties,
) {
    private val topic = kafkaProducerProperties.commandFeedback.topic

    fun sendReceivedFeedback(command: Command) {
        val commandFeedback = commandEntityToCommandFeedback(command, Received, "Command received")

        sendFeedback(commandFeedback)
    }

    fun sendCancellationFeedback(command: Command, message: String) {
        val commandFeedback = commandEntityToCommandFeedback(command, Cancelled, message)

        sendFeedback(commandFeedback)
    }

    fun sendRejectionFeedback(reason: String, command: ExternalCommand) {
        val commandFeedback = externalCommandToCommandFeedback(command, Rejected, reason)

        sendFeedback(commandFeedback)
    }

    fun sendProgressFeedback(currentCount: Int, totalCount: Int, command: Command) {
        val commandFeedback = commandEntityToCommandFeedback(command, Progress, "$currentCount/$totalCount")

        sendFeedback(commandFeedback)
    }

    fun sendSuccessFeedback(command: Command, feedback: String = "Command handled successfully") {
        val commandFeedback = commandEntityToCommandFeedback(command, Successful, feedback)
        sendFeedback(commandFeedback)
    }

    fun sendErrorFeedback(command: Command, error: String) {
        val commandFeedback = commandEntityToCommandFeedback(command, Error, error)
        sendFeedback(commandFeedback)
    }

    private fun sendFeedback(commandFeedback: CommandFeedback) {
        kafkaTemplate.send(topic, commandFeedback.deviceId, commandFeedback)
    }
}
