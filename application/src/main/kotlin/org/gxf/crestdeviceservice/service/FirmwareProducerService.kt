// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.Firmwares
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class FirmwareProducerService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    private val kafkaProducerProperties: KafkaProducerProperties
) {
    private val logger = KotlinLogging.logger {}

    fun send(firmwares: Firmwares) {
        logger.info { "Sending firmwares to Maki" }
        kafkaTemplate.send(kafkaProducerProperties.firmware.topic, firmwares)
    }
}
