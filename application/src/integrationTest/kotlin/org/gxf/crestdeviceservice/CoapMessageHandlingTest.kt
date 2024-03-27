// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.IntegrationTestHelper.getFileContentAsString
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        topics = ["\${crest-device-service.kafka.message-producer.topic-name}"],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CoapMessageHandlingTest {

    companion object {
        private const val IDENTITY = "1234"
        private const val PRE_SHARED_KEY_FIRST = "1234567890123456"
        private const val PRE_SHARED_KEY_NEW = "2345678901234567"
        private const val SECRET = "123456789"
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var pskRepository: PskRepository

    @BeforeEach
    fun setup() {
        pskRepository.save(
            PreSharedKey(
                IDENTITY,
                0,
                Instant.MIN,
                PRE_SHARED_KEY_FIRST,
                SECRET,
                PreSharedKeyStatus.ACTIVE
            )
        )
    }

    @AfterEach
    fun cleanup() {
        pskRepository.deleteAll()
    }

    @Test
    fun shouldReturnADownLinkContainingAPskSetCommandWhenTheKeyHasNotChangedYet() {
        pskRepository.save(
            PreSharedKey(
                IDENTITY,
                1,
                Instant.now(),
                PRE_SHARED_KEY_NEW,
                SECRET,
                PreSharedKeyStatus.READY
            )
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${IDENTITY}", request)

        assertThat(result.body).contains("PSK:", "SET")
    }

    @Test
    fun shouldChangeActiveKeyWhenTheKeyIsPendingAndSuccessURCReceived() {
        // pending psk, waiting for URC in next message from device
        pskRepository.save(
            PreSharedKey(
                IDENTITY,
                1,
                Instant.now(),
                PRE_SHARED_KEY_NEW,
                SECRET,
                PreSharedKeyStatus.PENDING
            )
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request =
            HttpEntity<String>(getFileContentAsString("message_psk_set_success.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${IDENTITY}", request)

        val oldKey = pskRepository.findFirstByIdentityOrderByRevisionAsc(IDENTITY)!!
        val newKey = pskRepository.findFirstByIdentityOrderByRevisionDesc(IDENTITY)!!

        assertThat(result.body).isEqualTo("0")
        assertThat(oldKey.status).isEqualTo(PreSharedKeyStatus.INACTIVE)
        assertThat(newKey.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
    }

    @Test
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived() {
        // pending psk, waiting for URC in next message from device
        pskRepository.save(
            PreSharedKey(
                IDENTITY,
                1,
                Instant.MIN,
                PRE_SHARED_KEY_NEW,
                SECRET,
                PreSharedKeyStatus.PENDING
            )
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request =
            HttpEntity<String>(getFileContentAsString("message_psk_set_failure.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${IDENTITY}", request)

        assertThat(result.body).isEqualTo("0")
        val oldKey = pskRepository.findFirstByIdentityOrderByRevisionAsc(IDENTITY)!!
        val newKey = pskRepository.findFirstByIdentityOrderByRevisionDesc(IDENTITY)!!

        assertThat(result.body).isEqualTo("0")
        assertThat(oldKey.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
        assertThat(newKey.status).isEqualTo(PreSharedKeyStatus.INVALID)
    }
}
