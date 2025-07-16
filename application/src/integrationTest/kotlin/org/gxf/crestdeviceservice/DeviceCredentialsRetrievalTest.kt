// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.repository.DeviceRepository
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.repository.PskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EmbeddedKafka(topics = ["\${kafka.producers.device-message.topic}"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DeviceCredentialsRetrievalTest {
    companion object {
        private const val IDENTITY = "1234"
        private const val PRE_SHARED_KEY = "1234567890123456"
        private const val SECRET = "123456789"
    }

    @Autowired private lateinit var restTemplate: TestRestTemplate

    @Autowired private lateinit var deviceRepository: DeviceRepository

    @Autowired private lateinit var pskRepository: PskRepository

    @BeforeEach
    fun setup() {
        deviceRepository.save(Device(IDENTITY, SECRET))
        pskRepository.save(PreSharedKey(IDENTITY, 0, Instant.MIN, PRE_SHARED_KEY, PreSharedKeyStatus.ACTIVE))
    }

    @AfterEach
    fun cleanup() {
        deviceRepository.deleteAll()
        pskRepository.deleteAll()
    }

    @Test
    fun shouldReturnTheLatestPskWhenThereAreMoreFoundForIdentity() {
        // create second PSK for identity this one should be returned
        pskRepository.save(PreSharedKey(IDENTITY, 1, Instant.MIN, "0000111122223333", PreSharedKeyStatus.ACTIVE))

        val headers = HttpHeaders().apply { add("x-device-identity", IDENTITY) }
        val result = restTemplate.exchange("/psk", HttpMethod.GET, HttpEntity<Unit>(headers), String::class.java)

        assertThat(result.body).isEqualTo("0000111122223333")
    }

    @Test
    fun shouldReturn404WhenNoKeyIsFound() {
        val headers = HttpHeaders().apply { add("x-device-identity", "12345") }
        val result = restTemplate.exchange("/psk", HttpMethod.GET, HttpEntity<Unit>(headers), String::class.java)

        assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
