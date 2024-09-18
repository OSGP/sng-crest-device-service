// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import org.gxf.crestdeviceservice.config.KafkaProducerProperties

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.sng.avro.DeviceMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

import java.time.Instant

/**
 * @param kafkaTemplate
 * @param kafkaProducerProperties
 */
@Service
class MessageProducerService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    private val kafkaProducerProperties: KafkaProducerProperties
) {
    private val logger = KotlinLogging.logger {}

    fun produceMessage(message: JsonNode) {
        logger.info { "Producing message for: ${message["ID"]}" }
        kafkaTemplate.send(
            kafkaProducerProperties.deviceMessage.topic,
            DeviceMessage().apply {
                deviceId = message["ID"].toString()
                timestamp = Instant.now().toEpochMilli()
                payload = message.toString()
            })
    }
}
