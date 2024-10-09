// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestConstants
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.service.PskService
import org.gxf.crestdeviceservice.service.command.CommandGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class DownlinkServiceTest {
    private val pskService = mock<PskService>()
    private val commandService = mock<CommandService>()
    private val messageProperties = MessageProperties(1024)
    private val pskCommandGenerator = mock<CommandGenerator>()
    private val pskSetCommandGenerator = mock<CommandGenerator>()
    private lateinit var downlinkService: DownlinkService
    private val deviceId = TestConstants.DEVICE_ID

    private val pskCommandString = "this-is-the-generated-PSK-command"
    private val pskSetCommandString = "this-is-the-generated-PSK:SET-command"

    @BeforeEach
    fun setUp() {
        whenever(pskCommandGenerator.getSupportedCommand()).thenReturn(Command.CommandType.PSK)
        whenever(pskCommandGenerator.generateCommandString(any())).thenReturn(pskCommandString)
        whenever(pskSetCommandGenerator.getSupportedCommand()).thenReturn(Command.CommandType.PSK_SET)
        whenever(pskSetCommandGenerator.generateCommandString(any())).thenReturn(pskSetCommandString)

        this.downlinkService =
            DownlinkService(
                pskService, commandService, messageProperties, listOf(pskCommandGenerator, pskSetCommandGenerator))
    }

    @Test
    fun shouldUseGeneratorWhenAvailableForCommand() {
        val pskCommandPending = CommandFactory.pendingPskCommand()
        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(listOf(pskCommandPending))
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(true)

        val downlink = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        verify(pskCommandGenerator).generateCommandString(pskCommandPending)
        assertThat(downlink).isEqualTo("!$pskCommandString")
    }

    @Test
    fun shouldUseCommandTypeDownlinkWhenGeneratorIsNotAvailable() {
        val rebootCommandPending = CommandFactory.pendingRebootCommand()
        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(listOf(rebootCommandPending))

        val downlink = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        verify(pskCommandGenerator, never()).generateCommandString(rebootCommandPending)
        assertThat(downlink).isEqualTo("!${Command.CommandType.REBOOT.downlink}")
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val pskCommand = CommandFactory.pendingPskCommand()
        val pskSetCommand = CommandFactory.pendingPskSetCommand()
        val pskCommandsPending = listOf(pskCommand, pskSetCommand)

        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(pskCommandsPending)
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(true)
        whenever(commandService.save(pskCommand)).thenReturn(pskCommand)
        whenever(commandService.save(pskSetCommand)).thenReturn(pskSetCommand)

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("!$pskCommandString;$pskSetCommandString")
        assertThat(pskCommand.status).isEqualTo(Command.CommandStatus.IN_PROGRESS)
        assertThat(pskSetCommand.status).isEqualTo(Command.CommandStatus.IN_PROGRESS)
    }

    @Test
    fun shouldSendPendingCommandsIfNoCommandInProgress() {
        val rebootCommand = CommandFactory.pendingRebootCommand()
        val pendingCommands = listOf(rebootCommand)
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)
        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(pendingCommands)
        whenever(commandService.getFirstCommandInProgressForDevice(deviceId)).thenReturn(null)
        whenever(commandService.save(rebootCommand)).thenReturn(rebootCommand)

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        val expectedDownlink = "!CMD:REBOOT"
        assertThat(result).isEqualTo(expectedDownlink)
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskAndACommandIsInProgress() {
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)
        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(listOf())
        whenever(commandService.getFirstCommandInProgressForDevice(deviceId))
            .thenReturn(CommandFactory.rebootCommandInProgress())

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskOrPendingCommand() {
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("0")
    }
}
