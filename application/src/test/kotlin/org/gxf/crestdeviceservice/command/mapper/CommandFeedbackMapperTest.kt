package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.CommandFeedbackFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import com.alliander.sng.CommandStatus as ExternalCommandStatus
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.TestHelper.MESSAGE_RECEIVED

import org.junit.jupiter.api.Test

class CommandFeedbackMapperTest {
    private val status = ExternalCommandStatus.Received
    private val expected = CommandFeedbackFactory.rebootCommandReceivedFeedback()

    @Test
    fun externalCommandToCommandFeedback() {
        val externalCommand = ExternalCommandFactory.externalRebootCommand()

        val result = CommandFeedbackMapper.externalCommandToCommandFeedback(externalCommand, status, MESSAGE_RECEIVED)

        assertThat(result).usingRecursiveComparison().ignoringFields("timestampStatus").isEqualTo(expected)
    }

    @Test
    fun commandEntityToCommandFeedback() {
        val commandEntity = CommandFactory.pendingRebootCommand()

        val result = CommandFeedbackMapper.commandEntityToCommandFeedback(commandEntity, status, MESSAGE_RECEIVED)

        assertThat(result).usingRecursiveComparison().ignoringFields("timestampStatus").isEqualTo(expected)
    }
}