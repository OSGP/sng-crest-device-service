// SPDX-FileCopyrightText: Contributors to the GXF project
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
data class Command(
    @Id @Generated val id: UUID,
    val deviceId: String,
    val correlationId: UUID,
    val timestampIssued: Instant,
    @Enumerated(EnumType.STRING) val type: CommandType,
    val commandValue: String?,
    @Enumerated(EnumType.STRING) var status: CommandStatus,
) {
    enum class CommandType(val downlink: String, val prefix: String, val urcsSuccess: List<String>, val urcsError: List<String>) {
        PSK(
            "PSK:SET",
            "PSK",
            listOf("PSK:SET"),
            listOf("PSK:DLER", "PSK:HSER", "PSK:EQER")),
        REBOOT(
            "CMD:REBOOT",
            "CMD",
            listOf("INIT", "WDR"),
            listOf())
    }

    enum class CommandStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESSFUL,
        ERROR,
        CANCELED
    }
}
