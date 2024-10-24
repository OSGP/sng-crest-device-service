// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.entity

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CommandTest {
    @ParameterizedTest
    @EnumSource(Command.CommandType::class)
    fun shouldCheckForValueGiven(commandType: Command.CommandType) {
        assertThat(createCommandWithValue(commandType, "some value")).isNotNull

        if (commandType.needsCommandValue) {
            assertThatThrownBy { createCommandWithValue(commandType, null) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    private fun createCommandWithValue(commandType: Command.CommandType, value: String?) =
        Command(
            UUID.randomUUID(),
            "some-device",
            UUID.randomUUID(),
            Instant.now(),
            commandType,
            value,
            Command.CommandStatus.PENDING)
}
