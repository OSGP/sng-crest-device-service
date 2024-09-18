// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.command.entity.Command

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus as ExternalCommandStatus

import java.time.Instant

object CommandFeedbackMapper {
    fun commandEntityToCommandFeedback(
        command: Command,
        status: ExternalCommandStatus,
        message: String
    ): CommandFeedback =
        CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()

    fun externalCommandToCommandFeedback(
        command: ExternalCommand,
        status: ExternalCommandStatus,
        message: String
    ): CommandFeedback =
        CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()
}
