// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.feedbackgenerator.CommandFeedbackGenerator
import org.gxf.crestdeviceservice.command.feedbackgenerator.InfoAlarmsFeedbackGenerator
import org.gxf.crestdeviceservice.command.resulthandler.CommandResultHandler
import org.gxf.crestdeviceservice.command.resulthandler.InfoAlarmsResultHandler
import org.gxf.crestdeviceservice.command.resulthandler.RebootCommandResultHandler
import org.gxf.crestdeviceservice.command.resulthandler.RspCommandResultHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CommandResultServiceTest {
    @MockK private lateinit var rebootCommandResultHandler: RebootCommandResultHandler
    @MockK private lateinit var rspCommandResultHandler: RspCommandResultHandler
    @MockK private lateinit var infoAlarmsResultHandler: InfoAlarmsResultHandler

    @MockK private lateinit var infoAlarmsFeedbackGenerator: InfoAlarmsFeedbackGenerator

    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandResultHandlersByType: Map<Command.CommandType, CommandResultHandler>
    @SpykBean private val commandFeedbackGenerators: MutableList<CommandFeedbackGenerator> = mutableListOf()

    @InjectMockKs private lateinit var commandResultService: CommandResultService

    @BeforeEach
    fun setUp() {
        every { commandResultHandlersByType[Command.CommandType.REBOOT] } answers { rebootCommandResultHandler }
        every { commandResultHandlersByType[Command.CommandType.RSP] } answers { rspCommandResultHandler }
        every { commandResultHandlersByType[Command.CommandType.INFO_ALARMS] } answers { infoAlarmsResultHandler }
        every { infoAlarmsFeedbackGenerator.supportedCommandType } answers { Command.CommandType.INFO_ALARMS }
        commandFeedbackGenerators.add(infoAlarmsFeedbackGenerator)
    }

    @Test
    fun shouldHandleMessageWhenCommandHasSucceeded() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rebootCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(command, message) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(command, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(command, message) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(command, message) }
    }

    @Test
    fun shouldHandleMessageWhenCommandHasFailed() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleFailure(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(command, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(command, message) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(command, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleFailure(command, message) }
    }

    @Test
    fun shouldHandleMessageWhenCommandIsStillInProgress() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns false
        justRun { rspCommandResultHandler.handleStillInProgress(any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(command, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(command, message) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(command, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleFailure(command, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleStillInProgress(command) }
    }

    @Test
    fun shouldHandleMessageWhenMultipleCommandsHaveSucceeded() {
        val message = MessageFactory.messageTemplate()
        val rebootCommand = CommandFactory.rebootCommandInProgress()
        val rspCommand = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(rebootCommand, rspCommand)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any(), any()) }
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleSuccess(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(rebootCommand, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(rebootCommand, message) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(rebootCommand, message) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(rebootCommand, message) }

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(rspCommand, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleSuccess(rspCommand, message) }
        verify(exactly = 0) { rspCommandResultHandler.hasFailed(rspCommand, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleFailure(rspCommand, message) }
    }

    @Test
    fun handleMessageWhenOneCommandHasSucceededAndOneCommandHasFailed() {
        val message = MessageFactory.messageTemplate()
        val rebootCommand = CommandFactory.rebootCommandInProgress()
        val rspCommand = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(rebootCommand, rspCommand)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any(), any()) }
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleFailure(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(rebootCommand, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(rebootCommand, message) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(rebootCommand, message) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(rebootCommand, message) }

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(rspCommand, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(rspCommand, message) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(rspCommand, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleFailure(rspCommand, message) }
    }

    @Test
    fun shouldHandleMessageWithCommandSpecificFeedback() {
        val downlink = ALARMS_INFO_DOWNLINK
        val message = MessageFactory.messageWithUrc(listOf(), downlink)
        val infoAlarmsCommand = CommandFactory.infoAlarmsCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(infoAlarmsCommand)
        every { infoAlarmsResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { infoAlarmsResultHandler.handleSuccess(any(), any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { infoAlarmsResultHandler.hasSucceeded(infoAlarmsCommand, message) }
        verify(exactly = 1) {
            infoAlarmsResultHandler.handleSuccess(infoAlarmsCommand, message, infoAlarmsFeedbackGenerator)
        }
        verify(exactly = 0) { infoAlarmsResultHandler.hasFailed(infoAlarmsCommand, message) }
        verify(exactly = 0) { infoAlarmsResultHandler.handleFailure(infoAlarmsCommand, message) }
    }
}
