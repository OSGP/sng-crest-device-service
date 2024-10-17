// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.io.File
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.IntegrationTestHelper.createKafkaConsumer
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.util.LinkedMultiValueMap

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = ["\${kafka.producers.firmware.topic}"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableConfigurationProperties(KafkaProducerProperties::class)
class FirmwareFileUploadTest {
    @Autowired private lateinit var restTemplate: TestRestTemplate
    @Autowired private lateinit var firmwareRepository: FirmwareRepository
    @Autowired private lateinit var firmwarePacketRepository: FirmwarePacketRepository
    @Autowired private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker
    @Autowired private lateinit var kafkaProducerProperties: KafkaProducerProperties

    private val name = "RTU#FULL#TO#23.10.txt"
    private val numberOfPackets = 13

    @Test
    fun firmwareFileUploadTest() {
        // arrange
        val firmwareFile = ClassPathResource(name).file
        val consumer = createKafkaConsumer(embeddedKafkaBroker, kafkaProducerProperties.firmware.topic)

        // act
        val response = uploadFile(firmwareFile)

        // assert
        assertThat(response.statusCode.value()).isEqualTo(302)
        assertThat(firmwareRepository.findByName(name)).isNotNull
        assertThat(firmwarePacketRepository.findAll().size).isEqualTo(numberOfPackets)

        val records = consumer.poll(Duration.ofSeconds(1))
        assertThat(records.records(kafkaProducerProperties.firmware.topic)).hasSize(1)
    }

    fun uploadFile(file: File): ResponseEntity<String> {
        val headers: HttpHeaders = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }

        val body: LinkedMultiValueMap<String, Any> =
            LinkedMultiValueMap<String, Any>().apply { add("file", FileSystemResource(file)) }
        val requestEntity = HttpEntity(body, headers)

        return this.restTemplate.postForEntity<String>("/web/firmware", requestEntity)
    }
}
