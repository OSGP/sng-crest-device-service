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

    enum class CommandType(
        val downlink: String,
        val urcsSuccess: List<String>,
        val urcsError: List<String>,
        val needsCommandValue: Boolean = false
    ) {
        PSK("PSK", listOf("PSK:TMP"), listOf("PSK:DLER", "PSK:HSER")),
        PSK_SET("PSK:SET", listOf("PSK:SET"), listOf("PSK:DLER", "PSK:HSER", "PSK:EQER")),
        REBOOT("CMD:REBOOT", listOf("INIT", "WDR"), listOf()),
        RSP("CMD:RSP", listOf("CMD:RSP"), listOf("DLER")),
        RSP2("CMD:RSP2", listOf("CMD:RSP2"), listOf("DLER")),
        FIRMWARE(
            "OTA",
            listOf("OTA:SUC"),
            listOf("OTA:CSER", "OTA:HSER", "OTA:RST", "OTA:SWNA", "OTA:FLER"),
            needsCommandValue = true),
    }

    enum class CommandStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESSFUL,
        ERROR,
        CANCELLED
    }
}
