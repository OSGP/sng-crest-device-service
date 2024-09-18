// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.avro.specific.SpecificRecordBase
import org.assertj.core.api.Assertions.assertThat
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
    private val deviceMessageTopic = "device-message"
    private val kafkaProducerProperties =
        KafkaProducerProperties(
            KafkaProducerTopicProperties(deviceMessageTopic),
            KafkaProducerTopicProperties("command-feedback"))
    @Mock private lateinit var mockedKafkaTemplate: KafkaTemplate<String, SpecificRecordBase>

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val messageProducerService =
            MessageProducerService(mockedKafkaTemplate, kafkaProducerProperties)

        val jsonNode =
            ObjectMapper()
                .readTree("""
            {
                "ID":12345
            }
        """)
        messageProducerService.produceMessage(jsonNode)
        verify(mockedKafkaTemplate)
            .send(
                check { assertThat(it).isEqualTo(deviceMessageTopic) },
                check { assertThat((it as DeviceMessage).payload).isEqualTo(jsonNode.toString()) })
    }
}
