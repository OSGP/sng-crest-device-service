// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.repository.CommandRepository

import com.alliander.sng.Command as AvroCommand
import com.alliander.sng.CommandFeedback as AvroCommandFeedback
import com.alliander.sng.CommandStatus as AvroCommandStatus
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext

import java.time.Duration
import java.time.Instant
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    topics = ["\${kafka.consumers.command.topic}", "\${kafka.producers.command-feedback.topic}"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MakiCommandHandlingTest {
    @Autowired private lateinit var commandRepository: CommandRepository
    @Autowired private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker
    @Value("\${kafka.consumers.command.topic}") private lateinit var commandTopic: String

    @Value("\${kafka.producers.command-feedback.topic}")
    private lateinit var commandFeedbackTopic: String

    @AfterEach
    fun cleanup() {
        commandRepository.deleteAll()
    }

    @Test
    fun `should save command with status pending and send received feedback when receiving command from Maki`() {
        // receiving reboot command from Maki
        val producer = IntegrationTestHelper.createKafkaProducer(embeddedKafkaBroker)
        val correlationId = UUID.randomUUID()
        val commandFromMaki = createCommandFromMaki(correlationId)
        val consumer =
            IntegrationTestHelper.createKafkaConsumer(embeddedKafkaBroker, commandFeedbackTopic)

        producer.send(ProducerRecord(commandTopic, commandFromMaki))

        // assert that received feedback is sent to Maki
        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5), 1)
        val actualFeedbackSent = records.records(commandFeedbackTopic).first().value()
        val expectedFeedbackSent = createCommandFeedbackToMaki(correlationId)

        assertThat(actualFeedbackSent)
            .usingRecursiveComparison()
            .ignoringFields("timestampStatus")
            .isEqualTo(expectedFeedbackSent)

        // assert that pending command has been saved
        waitUntilAssertedPendingCommandIsSaved()
    }

    private fun createCommandFromMaki(correlationId: UUID) = AvroCommand.newBuilder()
        .setDeviceId(DEVICE_ID)
        .setCorrelationId(correlationId)
        .setTimestamp(Instant.now())
        .setCommand("reboot")
        .setValue("")
        .build()

    private fun createCommandFeedbackToMaki(correlationId: UUID) = AvroCommandFeedback.newBuilder()
        .setDeviceId(DEVICE_ID)
        .setCorrelationId(correlationId)
        .setTimestampStatus(Instant.now())
        .setStatus(AvroCommandStatus.Received)
        .setMessage("Command received")
        .build()

    private fun waitUntilAssertedPendingCommandIsSaved() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted { assertPendingCommandIsSaved() }
    }

    private fun assertPendingCommandIsSaved() {
        val savedCommand =
            commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
                DEVICE_ID,
                CommandStatus.PENDING)

        assertThat(savedCommand).isNotNull
    }
    companion object {
        private const val DEVICE_ID = "1234"
    }
}
