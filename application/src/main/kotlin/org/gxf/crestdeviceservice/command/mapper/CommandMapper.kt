// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import java.util.UUID
import com.alliander.sng.Command as ExternalCommand

object CommandMapper {
    fun externalCommandToCommandEntity(externalCommand: ExternalCommand): Command {
        try {
            return Command(
                id = UUID.randomUUID(),
                deviceId = externalCommand.deviceId,
                correlationId = externalCommand.correlationId,
                timestampIssued = externalCommand.timestamp,
                type = commandNameToType(externalCommand.command),
                status = Command.CommandStatus.PENDING,
                commandValue = externalCommand.value,
            )
        } catch (exception: IllegalArgumentException) {
            throw CommandValidationException("Command unknown: ${externalCommand.command}")
        }
    }

    fun commandNameToType(command: String) = Command.CommandType.valueOf(command.uppercase())
}
