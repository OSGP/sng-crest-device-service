// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.CommandFeedbackFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.TestConstants.MESSAGE_RECEIVED

import com.alliander.sng.CommandStatus as ExternalCommandStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommandFeedbackMapperTest {
    private val status = ExternalCommandStatus.Received
    private val expected = CommandFeedbackFactory.rebootCommandReceivedFeedback()

    @Test
    fun externalCommandToCommandFeedback() {
        val externalCommand = ExternalCommandFactory.externalRebootCommand()

        val result =
            CommandFeedbackMapper.externalCommandToCommandFeedback(
                externalCommand, status, MESSAGE_RECEIVED)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("timestampStatus")
            .isEqualTo(expected)
    }

    @Test
    fun commandEntityToCommandFeedback() {
        val commandEntity = CommandFactory.pendingRebootCommand()

        val result =
            CommandFeedbackMapper.commandEntityToCommandFeedback(
                commandEntity, status, MESSAGE_RECEIVED)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("timestampStatus")
            .isEqualTo(expected)
    }
}
