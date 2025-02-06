// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.time.Instant
import java.util.UUID
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_MILLIBAR_PORT_3
import org.gxf.crestdeviceservice.TestConstants.CORRELATION_ID
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.TestConstants.timestamp
import org.gxf.crestdeviceservice.command.entity.Command

object CommandFactory {
    fun pendingPskCommand() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = CORRELATION_ID,
            timestampIssued = timestamp,
            type = Command.CommandType.PSK,
            commandValue = null,
            status = Command.CommandStatus.PENDING,
        )

    fun pendingPskSetCommand() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = CORRELATION_ID,
            timestampIssued = timestamp,
            type = Command.CommandType.PSK_SET,
            commandValue = null,
            status = Command.CommandStatus.PENDING,
        )

    fun pendingRebootCommand(
        timestampIssued: Instant = timestamp,
        correlationId: UUID = CORRELATION_ID,
        status: Command.CommandStatus = Command.CommandStatus.PENDING,
    ) =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = correlationId,
            timestampIssued = timestampIssued,
            type = Command.CommandType.REBOOT,
            commandValue = null,
            status = status,
        )

    fun pendingAnalogAlarmThresholdsCommand(
        timestampIssued: Instant = timestamp,
        correlationId: UUID = CORRELATION_ID,
        status: Command.CommandStatus = Command.CommandStatus.PENDING,
        value: String = ANALOG_ALARM_THRESHOLDS_MILLIBAR_PORT_3,
    ) =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = correlationId,
            timestampIssued = timestampIssued,
            type = Command.CommandType.ANALOG_ALARM_THRESHOLDS,
            commandValue = value,
            status = status,
        )

    fun analogAlarmThresholdsCommandInProgess(value: String = ANALOG_ALARM_THRESHOLDS_MILLIBAR_PORT_3) =
        pendingAnalogAlarmThresholdsCommand(value = value).start()

    fun infoAlarmsCommandInProgress(
        timestampIssued: Instant = timestamp,
        correlationId: UUID = CORRELATION_ID,
        status: Command.CommandStatus = Command.CommandStatus.PENDING,
    ) =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = correlationId,
            timestampIssued = timestampIssued,
            type = Command.CommandType.INFO_ALARMS,
            commandValue = null,
            status = status,
        )

    fun rebootCommandInProgress() = pendingRebootCommand().start()

    fun pskCommandInProgress() = pendingPskCommand().start()

    fun pskSetCommandInProgress() = pendingPskSetCommand().start()

    fun firmwareCommandInProgress() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = CORRELATION_ID,
            timestampIssued = timestamp,
            type = Command.CommandType.FIRMWARE,
            commandValue = "the-firmware-to-install",
            status = Command.CommandStatus.IN_PROGRESS,
        )

    fun rspCommandInProgress() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = CORRELATION_ID,
            timestampIssued = timestamp,
            type = Command.CommandType.RSP,
            commandValue = null,
            status = Command.CommandStatus.IN_PROGRESS,
        )

    fun rsp2CommandInProgress() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = CORRELATION_ID,
            timestampIssued = timestamp,
            type = Command.CommandType.RSP2,
            commandValue = null,
            status = Command.CommandStatus.IN_PROGRESS,
        )
}
