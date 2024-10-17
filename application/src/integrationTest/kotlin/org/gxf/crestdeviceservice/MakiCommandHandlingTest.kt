// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.Command
import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EmbeddedKafka(topics = ["\${kafka.consumers.command.topic}", "\${kafka.producers.command-feedback.topic}"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MakiCommandHandlingTest {
    companion object {
        private const val DEVICE_ID = "1234"
    }

    @Autowired private lateinit var commandRepository: CommandRepository

    @Autowired private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Value("\${kafka.consumers.command.topic}") private lateinit var commandTopic: String

    @Value("\${kafka.producers.command-feedback.topic}") private lateinit var commandFeedbackTopic: String

    @AfterEach
    fun cleanup() {
        commandRepository.deleteAll()
    }

    @Test
    fun shouldSaveCommandWithStatusPendingAndSendReceivedFeedbackWhenReceivingCommandFromMaki() {
        // receiving reboot command from Maki
        val producer = IntegrationTestHelper.createKafkaProducer(embeddedKafkaBroker)
        val correlationId = UUID.randomUUID()
        val commandFromMaki =
            Command.newBuilder()
                .setDeviceId(DEVICE_ID)
                .setCorrelationId(correlationId)
                .setTimestamp(Instant.now())
                .setCommand("reboot")
                .setValue("")
                .build()
        val consumer = IntegrationTestHelper.createKafkaConsumer(embeddedKafkaBroker, commandFeedbackTopic)

        producer.send(ProducerRecord(commandTopic, commandFromMaki))

        // assert that received feedback is sent to Maki
        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5), 1)
        val actualFeedbackSent = records.records(commandFeedbackTopic).first().value()
        val expectedFeedbackSent =
            CommandFeedback.newBuilder()
                .setDeviceId(DEVICE_ID)
                .setCorrelationId(correlationId)
                .setTimestampStatus(Instant.now())
                .setStatus(CommandStatus.Received)
                .setMessage("Command received")
                .build()

        assertThat(actualFeedbackSent)
            .usingRecursiveComparison()
            .ignoringFields("timestampStatus")
            .isEqualTo(expectedFeedbackSent)

        // assert that pending command has been saved
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted {
            val savedCommand =
                commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
                    DEVICE_ID, org.gxf.crestdeviceservice.command.entity.Command.CommandStatus.PENDING)

            assertThat(savedCommand).isNotNull
        }
    }
}
