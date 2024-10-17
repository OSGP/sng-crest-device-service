// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.CommandFeedback
import com.alliander.sng.Firmwares
import com.gxf.utilities.kafka.avro.AvroDeserializer
import com.gxf.utilities.kafka.avro.AvroSerializer
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.gxf.sng.avro.DeviceMessage
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
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
                AvroDeserializer(
                    listOf(
                        DeviceMessage.getClassSchema(), CommandFeedback.getClassSchema(), Firmwares.getClassSchema())))
        val consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic)
        return consumer
    }

    fun createKafkaProducer(embeddedKafkaBroker: EmbeddedKafkaBroker): Producer<String, SpecificRecordBase> {
        val producerProps: Map<String, Any> = HashMap(producerProps(embeddedKafkaBroker.brokersAsString))
        val producerFactory = DefaultKafkaProducerFactory(producerProps, StringSerializer(), AvroSerializer())
        return producerFactory.createProducer()
    }

    /**
     * Copy of the com.alliander.gxf.sngmessageprocessor.kafka test util producer props with different serializers
     *
     * @see KafkaTestUtils.producerProps
     */
    private fun producerProps(brokers: String): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ProducerConfig.BATCH_SIZE_CONFIG to "16384",
            ProducerConfig.LINGER_MS_CONFIG to 1,
            ProducerConfig.BUFFER_MEMORY_CONFIG to "33554432",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to AvroSerializer::class.java)
    }

    fun getFileContentAsString(path: String): String {
        return ResourceUtils.getFile("classpath:$path").readText()
    }
}
