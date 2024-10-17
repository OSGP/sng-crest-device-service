// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.avro.specific.SpecificRecordBase
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestConstants.COMMAND_FEEDBACK_TOPIC
import org.gxf.crestdeviceservice.TestConstants.DEVICE_MESSAGE_TOPIC
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_TOPIC
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.gxf.sng.avro.DeviceMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class MessageProducerServiceTest {

    @Mock private lateinit var mockedKafkaTemplate: KafkaTemplate<String, SpecificRecordBase>

    private val kafkaProducerProperties =
        KafkaProducerProperties(
            KafkaProducerTopicProperties(DEVICE_MESSAGE_TOPIC),
            KafkaProducerTopicProperties(COMMAND_FEEDBACK_TOPIC),
            KafkaProducerTopicProperties(FIRMWARE_TOPIC))

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val messageProducerService = MessageProducerService(mockedKafkaTemplate, kafkaProducerProperties)

        val jsonNode = ObjectMapper().readTree("""
            {
                "ID":12345
            }
        """)
        messageProducerService.produceMessage(jsonNode)
        verify(mockedKafkaTemplate)
            .send(
                check { assertThat(it).isEqualTo(DEVICE_MESSAGE_TOPIC) },
                check { assertThat((it as DeviceMessage).payload).isEqualTo(jsonNode.toString()) })
    }
}
