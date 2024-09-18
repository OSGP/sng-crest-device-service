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

/**
 * @property id
 * @property deviceId
 * @property correlationId
 * @property timestampIssued
 * @property type
 * @property commandValue
 * @property status
 */
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
    /**
     * @property downlink
     * @property urcsSuccess
     * @property urcsError
     */
    enum class CommandType(
        val downlink: String,
        val urcsSuccess: List<String>,
        val urcsError: List<String>
    ) {
        PSK(
            "PSK",
            listOf("PSK:TMP"),
            listOf("PSK:DLER", "PSK:HSER")
        ),
        PSK_SET(
            "PSK:SET",
            listOf("PSK:SET"),
            listOf("PSK:DLER", "PSK:HSER", "PSK:EQER")
        ),
        REBOOT(
            "CMD:REBOOT",
            listOf("INIT", "WDR"),
            listOf()
        ),
        ;
    }

    enum class CommandStatus {
        CANCELLED,
        ERROR,
        IN_PROGRESS,
        PENDING,
        SUCCESSFUL,
        ;
    }
}
