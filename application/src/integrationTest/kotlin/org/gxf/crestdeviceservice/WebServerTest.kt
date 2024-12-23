// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.io.File
import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.IntegrationTestHelper.createKafkaConsumer
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.repository.PskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.http.HttpMethod
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
class WebServerTest {
    @Autowired private lateinit var restTemplate: TestRestTemplate
    @Autowired private lateinit var firmwareRepository: FirmwareRepository
    @Autowired private lateinit var firmwarePacketRepository: FirmwarePacketRepository
    @Autowired private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker
    @Autowired private lateinit var kafkaProducerProperties: KafkaProducerProperties
    @Autowired private lateinit var pskRepository: PskRepository

    companion object {
        private const val NAME = "RTU#FULL#TO#23.10.txt"
        private const val NUMBER_OF_PACKETS = 13
        private const val IDENTITY = "1234"
        private const val PRE_SHARED_KEY = "1234567890123456"
    }

    @BeforeEach
    fun setup() {
        pskRepository.save(PreSharedKey(IDENTITY, 0, Instant.MIN, PRE_SHARED_KEY, PreSharedKeyStatus.ACTIVE))
    }

    @AfterEach
    fun cleanup() {
        pskRepository.deleteAll()
    }

    @Test
    fun firmwareFileUploadTest() {
        // arrange
        val firmwareFile = ClassPathResource(NAME).file
        val consumer = createKafkaConsumer(embeddedKafkaBroker, kafkaProducerProperties.firmware.topic)

        // act
        val response = uploadFile(firmwareFile)

        // assert
        assertThat(response.statusCode.value()).isEqualTo(200)
        assertThat(firmwareRepository.findByName(NAME)).isNotNull
        assertThat(firmwarePacketRepository.findAll()).hasSize(NUMBER_OF_PACKETS)

        val records = consumer.poll(Duration.ofSeconds(1))
        assertThat(records.records(kafkaProducerProperties.firmware.topic)).hasSize(1)
    }

    fun uploadFile(file: File): ResponseEntity<String> {
        val headers: HttpHeaders = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }

        val body = LinkedMultiValueMap<String, Any>().apply { add("file", FileSystemResource(file)) }
        val requestEntity = HttpEntity(body, headers)

        return this.restTemplate.postForEntity<String>("/web/firmware", requestEntity)
    }

    @Test
    fun pskRequestOnWebPortShouldReturn404() {
        // create second PSK for identity this one should be returned
        pskRepository.save(PreSharedKey(IDENTITY, 1, Instant.MIN, "0000111122223333", PreSharedKeyStatus.ACTIVE))

        val headers = HttpHeaders().apply { add("x-device-identity", IDENTITY) }
        val result = restTemplate.exchange("/psk", HttpMethod.GET, HttpEntity<Unit>(headers), String::class.java)

        assertThat(result.statusCode.is4xxClientError).isTrue()
    }
}
