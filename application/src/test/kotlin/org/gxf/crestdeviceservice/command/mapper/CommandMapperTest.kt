// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class CommandMapperTest {
    @Test
    fun externalCommandToCommandEntity() {
        val externalCommand = ExternalCommandFactory.externalRebootCommand()
        val status = Command.CommandStatus.PENDING
        val expected = CommandFactory.pendingRebootCommand()

        val result = CommandMapper.externalCommandToCommandEntity(externalCommand, status)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("id", "timestampIssued")
            .isEqualTo(expected)
    }

    @Test
    fun externalCommandToCommandEntityException() {
        val externalCommand = ExternalCommandFactory.externalRebootCommandInvalid()
        val status = Command.CommandStatus.PENDING

        val actual = catchThrowable {
            CommandMapper.externalCommandToCommandEntity(externalCommand, status)
        }

        assertThat(actual)
            .isInstanceOf(CommandValidationException::class.java)
            .hasMessage("Command unknown: ${externalCommand.command}")
    }

    @Test
    fun commandNameToType() {
        val commandName = "reboot"
        val result = CommandMapper.commandNameToType(commandName)

        assertThat(result).isEqualTo(Command.CommandType.REBOOT)
    }

    @Test
    fun commandNameToTypeException() {
        val commandName = "invalid"

        val actual = catchThrowable { CommandMapper.commandNameToType(commandName) }

        assertThat(actual).isInstanceOf(IllegalArgumentException::class.java)
    }
}
