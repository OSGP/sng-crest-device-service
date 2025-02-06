// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.io.IOException
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.feedbackgenerator.InfoAlarmsFeedbackGenerator
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class InfoAlarmsResultHandlerTest {
    @MockK private lateinit var infoAlarmsFeedbackGenerator: InfoAlarmsFeedbackGenerator

    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService
    @MockK private lateinit var alarmsInfoService: AlarmsInfoService

    @InjectMockKs private lateinit var resultHandler: InfoAlarmsResultHandler

    private lateinit var command: Command
    private lateinit var message: JsonNode

    @BeforeEach
    fun setup() {
        command = CommandFactory.infoAlarmsCommandInProgress()
        val urcs = listOf<String>()
        val downlink = ALARMS_INFO_DOWNLINK
        message = MessageFactory.messageWithUrc(urcs, downlink)
    }

    @Test
    fun supportedCommandType() {
        val result = resultHandler.supportedCommandType

        assertThat(result).isEqualTo(Command.CommandType.INFO_ALARMS)
    }

    @Test
    fun hasSucceeded() {
        every { alarmsInfoService.getAlarmsInfo(any()) } returns AlarmsInfo()

        val result = resultHandler.hasSucceeded(command, message)

        assertThat(result).isTrue()
    }

    @Test
    fun hasSucceededFails() {
        every { alarmsInfoService.getAlarmsInfo(any()) } throws IOException()

        val result = resultHandler.hasSucceeded(command, message)

        assertThat(result).isFalse()
    }

    @ParameterizedTest
    @MethodSource("hasFailedTestSource")
    fun hasFailed(urcs: List<String>, downlink: String, expected: Boolean) {
        val messageToFail = MessageFactory.messageWithUrc(urcs, downlink)

        val result = resultHandler.hasFailed(command, messageToFail)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun handleSuccess() {
        val generatedFeedback = "feedback"
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any(), any()) }
        every { infoAlarmsFeedbackGenerator.generateFeedback(any()) } answers { generatedFeedback }

        resultHandler.handleSuccess(command, message, infoAlarmsFeedbackGenerator)

        assertThat(command.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command, generatedFeedback) }
    }

    @Test
    fun handleFailure() {
        val failureMessage = MessageFactory.messageWithUrc(listOf("INFO:DLER"))

        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendErrorFeedback(any(), any()) }

        resultHandler.handleFailure(command, failureMessage)

        assertThat(command.status).isEqualTo(Command.CommandStatus.ERROR)
        verify { commandService.saveCommand(command) }
        verify {
            commandFeedbackService.sendErrorFeedback(
                command,
                match { error -> error.contains("Downlink (syntax) error") },
            )
        }
    }

    companion object {
        @JvmStatic
        fun hasFailedTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("AL6:SET"), "0", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("INFO:DLER"), "0", true),
                Arguments.of(listOf("INFO:ERR"), "0", true),
                Arguments.of(listOf("INFO:DLNA", "INIT"), "0", true),
            )
    }
}
