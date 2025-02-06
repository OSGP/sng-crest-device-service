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
class RebootCommandResultHandlerTest {
    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var rebootCommandResultHandler: RebootCommandResultHandler

    @Test
    fun handleSuccess() {
        val command = CommandFactory.rebootCommandInProgress()
        val message = MessageFactory.messageWithUrc(listOf("INIT"))
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        rebootCommandResultHandler.handleSuccess(command, message)

        assertThat(command.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command) }
    }

    @ParameterizedTest
    @MethodSource("hasSucceededTestSource")
    fun hasSucceeded(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        val hasSucceeded = rebootCommandResultHandler.hasSucceeded(CommandFactory.rebootCommandInProgress(), message)

        assertThat(hasSucceeded).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("hasFailedTestSource")
    fun hasFailed(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        val hasFailed = rebootCommandResultHandler.hasFailed(CommandFactory.rebootCommandInProgress(), message)

        assertThat(hasFailed).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun hasSucceededTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("INIT", "WDR"), "0", true),
                Arguments.of(listOf("INIT"), "0", true),
                Arguments.of(listOf("WDR"), "0", false),
                Arguments.of(listOf("PSK:TMP", "PSK:SET"), "!PSK:######;PSK:######SET", false),
            )

        @JvmStatic
        fun hasFailedTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("INIT"), "0", false),
                Arguments.of(listOf("WDR"), "0", false),
                Arguments.of(listOf("PSK:DLER"), "!PSK:#####", false),
                Arguments.of(listOf("PSK:HSER"), "0", false),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######", false),
                Arguments.of(listOf("PSK:SET"), "!PSK:#####SET", false),
            )
    }
}
