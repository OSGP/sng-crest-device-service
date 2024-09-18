// SPDX-FileCopyrightText: Contributors to the GXF project
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
import org.mockito.kotlin.refEq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CommandConsumerTest {
    val commandService: CommandService = mock()
    val commandFeedbackService: CommandFeedbackService = mock()
    val pskService: PskService = mock()
    val commandConsumer = CommandConsumer(commandService, commandFeedbackService, pskService)
    private val externalCommand = ExternalCommandFactory.externalRebootCommand()
    private val command = CommandFactory.pendingRebootCommand()

    @Test
    fun rebootCommandSaved() {
        whenever(commandService.isPskCommand(command)).thenReturn(false)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).validate(refEq(command, "id", "timestampIssued"))
        verify(commandFeedbackService).sendReceivedFeedback(refEq(command, "id", "timestampIssued"))
        verify(commandService)
            .cancelOlderCommandIfNecessary(refEq(command, "id", "timestampIssued"))
        verify(commandService).save(refEq(command, "id", "timestampIssued"))
    }

    @Test
    fun pskCommandSavedAndKeyGenerated() {
        whenever(commandService.isPskCommand(any<Command>())).thenReturn(true)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).validate(refEq(command, "id", "timestampIssued"))
        verify(commandFeedbackService).sendReceivedFeedback(refEq(command, "id", "timestampIssued"))
        verify(commandService)
            .cancelOlderCommandIfNecessary(refEq(command, "id", "timestampIssued"))
        verify(pskService).generateNewReadyKeyForDevice(command.deviceId)
        verify(commandService).save(refEq(command, "id", "timestampIssued"))
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        val command = ExternalCommandFactory.externalRebootCommand().apply {
            command = "UNKNOWN"
        }

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
        verify(commandFeedbackService, times(0)).sendReceivedFeedback(any<Command>())
        verify(commandService, times(0)).save(any<Command>())
    }
}
