// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CommandServiceTest {
    private val commandRepository = mock<CommandRepository>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val commandService = CommandService(commandRepository, commandFeedbackService)

    @Test
    fun validateSucceeded() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(CommandFactory.pendingRebootCommand(Instant.now().minusSeconds(100)))

        assertDoesNotThrow { commandService.validate(CommandFactory.pendingRebootCommand()) }
    }

    @Test
    fun `Check if command is rejected when latest same command is in the future`() {
        val exceptionExpected = CommandValidationException("There is a newer command of the same type")

        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(CommandFactory.pendingRebootCommand(Instant.now().plusSeconds(100)))

        assertThatThrownBy { commandService.validate(CommandFactory.pendingRebootCommand()) }
            .usingRecursiveComparison()
            .isEqualTo(exceptionExpected)
    }

    @Test
    fun `Check if command is rejected when same command is in progress`() {
        val exceptionExpected = CommandValidationException("A command of the same type is already in progress.")
        whenever(commandRepository.findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(any(), any(), any()))
            .thenReturn(listOf(CommandFactory.rebootCommandInProgress()))

        assertThatThrownBy { commandService.validate(CommandFactory.pendingRebootCommand()) }
            .usingRecursiveComparison()
            .isEqualTo(exceptionExpected)
    }

    @Test
    fun `Check if existing pending same command is cancelled if it exists`() {
        val newCommand = CommandFactory.pendingRebootCommand()
        val existingPendingCommand =
            CommandFactory.pendingRebootCommand(
                timestampIssued = Instant.now().minusSeconds(100), correlationId = UUID.randomUUID())

        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(existingPendingCommand)
        whenever(commandRepository.save(any<Command>())).thenReturn(existingPendingCommand)

        commandService.cancelOlderCommandIfNecessary(newCommand)

        verify(commandFeedbackService).sendCancellationFeedback(eq(existingPendingCommand), any<String>())
        verify(commandRepository).save(existingPendingCommand)
        assertThat(existingPendingCommand.status).isEqualTo(Command.CommandStatus.CANCELLED)
    }

    @Test
    fun `Check if no command is cancelled if no existing same pending command exists`() {
        val newCommand = CommandFactory.pendingRebootCommand()
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any())).thenReturn(null)

        commandService.cancelOlderCommandIfNecessary(newCommand)

        verify(commandFeedbackService, never()).sendCancellationFeedback(any<Command>(), any<String>())
    }
}
