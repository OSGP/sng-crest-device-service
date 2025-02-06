// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.entity

import jakarta.annotation.Generated
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.Instant
import java.util.UUID

@Entity
class Command(
    @Id @Generated val id: UUID,
    val deviceId: String,
    val correlationId: UUID,
    val timestampIssued: Instant,
    @Enumerated(EnumType.STRING) val type: CommandType,
    val commandValue: String?,
    status: CommandStatus,
) {
    init {
        if (type.needsCommandValue) {
            checkNotNull(commandValue)
        }
    }

    fun getRequiredCommandValue() = checkNotNull(commandValue)

    @Enumerated(EnumType.STRING)
    var status: CommandStatus = status
        private set

    fun start() = apply { status = CommandStatus.IN_PROGRESS }

    fun finish() = apply { status = CommandStatus.SUCCESSFUL }

    fun fail() = apply { status = CommandStatus.ERROR }

    fun cancel() = apply { status = CommandStatus.CANCELLED }

    enum class CommandType(val downlink: String, val needsCommandValue: Boolean = false) {
        ANALOG_ALARM_THRESHOLDS("AL", needsCommandValue = true),
        INFO_ALARMS("INFO:ALARMS"),
        PSK("PSK"),
        PSK_SET("PSK:SET"),
        REBOOT("CMD:REBOOT"),
        RSP("CMD:RSP"),
        RSP2("CMD:RSP2"),
        FIRMWARE("OTA", needsCommandValue = true),
    }

    enum class CommandStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESSFUL,
        ERROR,
        CANCELLED,
    }
}
