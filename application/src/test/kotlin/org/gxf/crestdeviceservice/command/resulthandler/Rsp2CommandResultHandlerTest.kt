// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class Rsp2CommandResultHandlerTest {
    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var rsp2CommandResultHandler: Rsp2CommandResultHandler

    @Test
    fun handleSuccess() {
        val command = CommandFactory.rsp2CommandInProgress()
        val message = MessageFactory.messageWithUrc(listOf(rsp2CommandResultHandler.successUrc))
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        rsp2CommandResultHandler.handleSuccess(command, message)

        assertThat(command.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command) }
    }

    @Test
    fun handleFailure() {
        val command = CommandFactory.rsp2CommandInProgress()
        val message = MessageFactory.messageWithUrc(listOf("PSK:HSER"))
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendErrorFeedback(any(), any()) }

        rsp2CommandResultHandler.handleFailure(command, message)

        assertThat(command.status).isEqualTo(Command.CommandStatus.ERROR)
        verify { commandService.saveCommand(command) }
        verify {
            commandFeedbackService.sendErrorFeedback(command, match { error -> error.contains("SHA256 hash error") })
        }
    }

    @ParameterizedTest
    @MethodSource("hasSucceededTestSource")
    fun hasSucceeded(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        val hasSucceeded = rsp2CommandResultHandler.hasSucceeded(CommandFactory.rsp2CommandInProgress(), message)

        assertThat(hasSucceeded).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("hasFailedTestSource")
    fun hasFailed(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        val hasFailed = rsp2CommandResultHandler.hasFailed(CommandFactory.rsp2CommandInProgress(), message)

        assertThat(hasFailed).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun hasSucceededTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("RSP2:OK"), "CMD:RSP2", true),
                Arguments.of(listOf<String>(), "CMD:RSP2", false),
                Arguments.of(listOf("PSK:TMP", "RSP2:OK"), "!PSK:######;CMD:RSP2", true),
                Arguments.of(listOf("RSP2:OK", "PSK:TMP"), "!CMD:RSP2;PSK:######", true),
                Arguments.of(listOf("RSP2:DLER"), "CMD:RSP2", false),
                Arguments.of(listOf("RSP2:DLER", "PSK:TMP"), "!CMD:RSP2;PSK:######", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("PSK:SET"), "0", false),
                Arguments.of(listOf("PSK:TMP", "PSK:SET"), "0", false),
            )

        @JvmStatic
        fun hasFailedTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("RSP2:DLER"), "CMD:RSP2", true),
                Arguments.of(listOf("RSP2:DLER"), "CMD:RSP2", true),
                Arguments.of(listOf("RSP2:DLER", "PSK:TMP"), "!CMD:RSP2;PSK:######", true),
                Arguments.of(listOf<String>(), "CMD:RSP2", false),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######;CMD:RSP2", false),
                Arguments.of(listOf("PSK:TMP"), "!CMD:RSP2;PSK:######", false),
                Arguments.of(listOf("PSK:DLER"), "!PSK:#####", false),
                Arguments.of(listOf("PSK:HSER"), "0", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######", false),
                Arguments.of(listOf("PSK:SET"), "!PSK:#####SET", false),
            )
    }
}
