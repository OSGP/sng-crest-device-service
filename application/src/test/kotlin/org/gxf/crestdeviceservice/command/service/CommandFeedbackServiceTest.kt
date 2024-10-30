// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import org.apache.avro.specific.SpecificRecordBase
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory.firmwareCommandInProgress
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class CommandFeedbackServiceTest {
    private val topic = "topic"

    @Mock private lateinit var kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private lateinit var kafkaProducerProperties: KafkaProducerProperties

    private lateinit var service: CommandFeedbackService

    @Captor private lateinit var feedbackCaptor: ArgumentCaptor<CommandFeedback>

    @BeforeEach
    fun createService() {
        whenever(kafkaProducerProperties.commandFeedback.topic).thenReturn(topic)

        service = CommandFeedbackService(kafkaTemplate, kafkaProducerProperties)
    }

    @Test
    fun `should send progress`() {
        service.sendProgressFeedback(2, 5, firmwareCommandInProgress())

        verify(kafkaTemplate).send(eq(topic), eq(DEVICE_ID), capture(feedbackCaptor))

        val feedback = feedbackCaptor.value
        assertThat(feedback.deviceId).isEqualTo(DEVICE_ID)
        assertThat(feedback.status).isEqualTo(CommandStatus.Progress)
        assertThat(feedback.message).isEqualTo("2/5")
    }
}
