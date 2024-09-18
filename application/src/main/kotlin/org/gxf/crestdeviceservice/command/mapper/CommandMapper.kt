// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException

import com.alliander.sng.Command as ExternalCommand

import java.util.UUID

object CommandMapper {
    fun externalCommandToCommandEntity(
        externalCommand: ExternalCommand,
        status: Command.CommandStatus
    ): Command {
        try {
            return Command(
                id = UUID.randomUUID(),
                deviceId = externalCommand.deviceId,
                correlationId = externalCommand.correlationId,
                timestampIssued = externalCommand.timestamp,
                type = commandNameToType(externalCommand.command),
                status = status,
                commandValue = externalCommand.value)
        } catch (exception: IllegalArgumentException) {
            throw CommandValidationException("Command unknown: ${externalCommand.command}")
        }
    }

    fun commandNameToType(command: String) = Command.CommandType.valueOf(command.uppercase())
}
