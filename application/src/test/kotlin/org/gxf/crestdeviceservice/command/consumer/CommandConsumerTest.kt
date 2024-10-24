// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.consumer

import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CommandConsumerTest {
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val pskService = mock<PskService>()
    private val commandConsumer = CommandConsumer(commandService, commandFeedbackService, pskService)

    private val externalCommand = ExternalCommandFactory.externalRebootCommand()
    private val command = CommandFactory.pendingRebootCommand()

    @Test
    fun rebootCommandSaved() {
        whenever(commandService.isPskCommand(command)).thenReturn(false)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).validate(refEq(command, "id", "timestampIssued"))
        verify(commandFeedbackService).sendReceivedFeedback(refEq(command, "id", "timestampIssued"))
        verify(commandService).cancelOlderCommandIfNecessary(refEq(command, "id", "timestampIssued"))
        verify(commandService).saveCommand(refEq(command, "id", "timestampIssued"))
    }

    @Test
    fun pskCommandSavedAndKeyGenerated() {
        whenever(commandService.isPskCommand(any<Command>())).thenReturn(true)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).validate(refEq(command, "id", "timestampIssued"))
        verify(commandFeedbackService).sendReceivedFeedback(refEq(command, "id", "timestampIssued"))
        verify(commandService).cancelOlderCommandIfNecessary(refEq(command, "id", "timestampIssued"))
        verify(pskService).generateNewReadyKeyForDevice(command.deviceId)
        verify(commandService).saveCommand(refEq(command, "id", "timestampIssued"))
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        val command = ExternalCommandFactory.externalRebootCommand()
        command.command = "UNKNOWN"

        commandConsumer.handleIncomingCommand(command)

        verify(commandFeedbackService).sendRejectionFeedback("Command unknown: UNKNOWN", command)
    }

    @Test
    fun commandRejectedForOtherReasons() {
        val reason = "There is a newer command of the same type"
        val commandValidationException = CommandValidationException(reason)
        whenever(commandService.validate(any<Command>())).thenThrow(commandValidationException)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandFeedbackService).sendRejectionFeedback(reason, externalCommand)
        verify(commandFeedbackService, never()).sendReceivedFeedback(any<Command>())
        verify(commandService, never()).saveCommand(any<Command>())
    }
}
