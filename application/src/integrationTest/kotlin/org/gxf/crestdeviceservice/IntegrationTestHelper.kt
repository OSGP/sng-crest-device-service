// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.gxf.utilities.kafka.avro.AvroDeserializer
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.gxf.sng.avro.DeviceMessage
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.util.ResourceUtils

object IntegrationTestHelper {

    fun createKafkaConsumer(
        embeddedKafkaBroker: EmbeddedKafkaBroker,
        topic: String
    ): Consumer<String, SpecificRecordBase> {
        val testProperties = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker)
        val consumerFactory =
            DefaultKafkaConsumerFactory(
                testProperties,
                StringDeserializer(),
                AvroDeserializer(listOf(DeviceMessage.getClassSchema())))
        val consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic)
        return consumer
    }

    fun getFileContentAsString(path: String): String {
        return ResourceUtils.getFile("classpath:$path").readText()
    }
}
