// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.apache.avro.specific.SpecificRecordBase
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory.analogAlarmThresholdsCommandInProgess
import org.gxf.crestdeviceservice.CommandFactory.firmwareCommandInProgress
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockKExtension::class)
class CommandFeedbackServiceTest {
    @MockK(relaxed = true) private lateinit var kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>
    @MockK private lateinit var kafkaProducerProperties: KafkaProducerProperties
    @MockK private lateinit var commandFeedbackProperties: KafkaProducerTopicProperties

    private lateinit var service: CommandFeedbackService

    private val topic = "topic"

    @BeforeEach
    fun createService() {
        every { kafkaProducerProperties.commandFeedback } returns commandFeedbackProperties
        every { commandFeedbackProperties.topic } returns topic

        service = CommandFeedbackService(kafkaTemplate, kafkaProducerProperties)
    }

    @Test
    fun `should send progress`() {
        service.sendProgressFeedback(2, 5, firmwareCommandInProgress())

        val feedbackSlot = slot<CommandFeedback>()

        verify { kafkaTemplate.send(topic, DEVICE_ID, capture(feedbackSlot)) }

        val feedback = feedbackSlot.captured
        assertThat(feedback.deviceId).isEqualTo(DEVICE_ID)
        assertThat(feedback.status).isEqualTo(CommandStatus.Progress)
        assertThat(feedback.message).isEqualTo("2/5")
    }

    @Test
    fun `should send default success message`() {
        service.sendSuccessFeedback(analogAlarmThresholdsCommandInProgess())

        val feedbackSlot = slot<CommandFeedback>()

        verify { kafkaTemplate.send(topic, DEVICE_ID, capture(feedbackSlot)) }

        val feedback = feedbackSlot.captured
        assertThat(feedback.deviceId).isEqualTo(DEVICE_ID)
        assertThat(feedback.status).isEqualTo(CommandStatus.Successful)
        assertThat(feedback.message).isEqualTo("Command handled successfully")
    }

    @Test
    fun `should send generated success message`() {
        val feedbackMessage = "feedback"

        service.sendSuccessFeedback(analogAlarmThresholdsCommandInProgess(), feedbackMessage)

        val feedbackSlot = slot<CommandFeedback>()

        verify { kafkaTemplate.send(topic, DEVICE_ID, capture(feedbackSlot)) }

        val feedback = feedbackSlot.captured
        assertThat(feedback.deviceId).isEqualTo(DEVICE_ID)
        assertThat(feedback.status).isEqualTo(CommandStatus.Successful)
        assertThat(feedback.message).isEqualTo(feedbackMessage)
    }
}
