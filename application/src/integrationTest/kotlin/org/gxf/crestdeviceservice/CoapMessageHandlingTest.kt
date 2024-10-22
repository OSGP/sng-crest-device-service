// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.gxf.crestdeviceservice.IntegrationTestHelper.getFileContentAsString
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.repository.DeviceRepository
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.repository.PskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = ["\${kafka.producers.command-feedback.topic}"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CoapMessageHandlingTest {
    companion object {
        private const val DEVICE_ID = "1234"
        private const val PRE_SHARED_KEY_FIRST = "1234567890123456"
        private const val PRE_SHARED_KEY_NEW = "2345678901234567"
        private const val SECRET = "123456789"
    }

    @Autowired private lateinit var restTemplate: TestRestTemplate

    @Autowired private lateinit var deviceRepository: DeviceRepository

    @Autowired private lateinit var pskRepository: PskRepository

    @Autowired private lateinit var commandRepository: CommandRepository

    @Autowired private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Value("\${kafka.producers.command-feedback.topic}") private lateinit var commandFeedbackTopic: String

    @BeforeEach
    fun setup() {
        deviceRepository.save(Device(DEVICE_ID, SECRET))
        pskRepository.save(PreSharedKey(DEVICE_ID, 0, Instant.MIN, PRE_SHARED_KEY_FIRST, PreSharedKeyStatus.ACTIVE))
    }

    @AfterEach
    fun cleanup() {
        deviceRepository.deleteAll()
        pskRepository.deleteAll()
        commandRepository.deleteAll()
    }

    @Test
    fun shouldReturnADownLinkContainingPskCommands() {
        pskRepository.save(PreSharedKey(DEVICE_ID, 1, Instant.now(), PRE_SHARED_KEY_NEW, PreSharedKeyStatus.READY))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK,
                null,
                Command.CommandStatus.PENDING))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK_SET,
                null,
                Command.CommandStatus.PENDING))

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${DEVICE_ID}", request)

        assertThat(result.body).contains("PSK", "SET")
    }

    @Test
    fun shouldChangeActiveKey() {
        // pending psk, waiting for URC in next message from device
        pskRepository.save(PreSharedKey(DEVICE_ID, 1, Instant.now(), PRE_SHARED_KEY_NEW, PreSharedKeyStatus.PENDING))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK,
                null,
                Command.CommandStatus.IN_PROGRESS))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK_SET,
                null,
                Command.CommandStatus.IN_PROGRESS))

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message_psk_set_success.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${DEVICE_ID}", request)

        val oldKey = pskRepository.findFirstByIdentityOrderByRevisionAsc(DEVICE_ID)!!
        val newKey = pskRepository.findFirstByIdentityOrderByRevisionDesc(DEVICE_ID)!!

        assertThat(result.body).isEqualTo("0")
        assertThat(oldKey.status).isEqualTo(PreSharedKeyStatus.INACTIVE)
        assertThat(newKey.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
    }

    @Test
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived() {
        // pending psk, waiting for URC in next message from device
        pskRepository.save(PreSharedKey(DEVICE_ID, 1, Instant.MIN, PRE_SHARED_KEY_NEW, PreSharedKeyStatus.PENDING))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK,
                null,
                Command.CommandStatus.IN_PROGRESS))
        commandRepository.save(
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.PSK_SET,
                null,
                Command.CommandStatus.IN_PROGRESS))

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message_psk_set_failure.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${DEVICE_ID}", request)

        assertThat(result.body).isEqualTo("0")
        val oldKey = pskRepository.findFirstByIdentityOrderByRevisionAsc(DEVICE_ID)!!
        val newKey = pskRepository.findFirstByIdentityOrderByRevisionDesc(DEVICE_ID)!!

        assertThat(result.body).isEqualTo("0")
        assertThat(oldKey.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
        assertThat(newKey.status).isEqualTo(PreSharedKeyStatus.INVALID)
    }

    @Test
    fun shouldSendCommandInDownlinkAndSetStatusToInProgressWhenReceivingAMessageFromDevice() {
        // pending command, waiting for URC in next message from device
        val pendingCommand =
            Command(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                Instant.now(),
                Command.CommandType.REBOOT,
                null,
                Command.CommandStatus.PENDING)
        commandRepository.save(pendingCommand)

        // receiving message from device
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${DEVICE_ID}", request)

        // downlink sent to device
        assertThat(result.body).isEqualTo("!CMD:REBOOT")

        // check if reboot command is in database with status IN_PROGRESS
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted {
            val savedCommand =
                commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
                    DEVICE_ID, Command.CommandStatus.IN_PROGRESS)

            assertThat(savedCommand).isNotNull()
        }
    }

    @Test
    fun shouldSendCommandSuccessFeedbackToMaki() {
        val consumer = IntegrationTestHelper.createKafkaConsumer(embeddedKafkaBroker, commandFeedbackTopic)

        // command in progress should be in database
        val id = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val commandInProgress =
            Command(
                id,
                DEVICE_ID,
                correlationId,
                Instant.now(),
                Command.CommandType.REBOOT,
                "reboot",
                Command.CommandStatus.IN_PROGRESS)
        commandRepository.save(commandInProgress)

        // receiving message from device
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message_reboot.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${DEVICE_ID}", request)

        assertThat(result.body).isEqualTo("0")

        // check if reboot command is in database with status SUCCESSFUL
        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted {
            val savedCommand =
                commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
                    DEVICE_ID, Command.CommandStatus.SUCCESSFUL)

            assertThat(savedCommand).isNotNull
        }

        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2), 1)

        val actualFeedbackSent = records.records(commandFeedbackTopic).first().value()
        val expectedFeedbackSent =
            CommandFeedback.newBuilder()
                .setDeviceId(DEVICE_ID)
                .setCorrelationId(correlationId)
                .setTimestampStatus(Instant.now())
                .setStatus(CommandStatus.Successful)
                .setMessage("Command handled successfully")
                .build()

        assertThat(actualFeedbackSent)
            .usingRecursiveComparison()
            .ignoringFields("timestampStatus")
            .isEqualTo(expectedFeedbackSent)
    }
}
