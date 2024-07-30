// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.entity

import jakarta.annotation.Generated
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Table
data class Command(
    @Id @Generated val id: UUID,
    val deviceId: String,
    val correlationId: UUID,
    val timestampIssued: Instant,
    @Enumerated(EnumType.STRING) val type: CommandType,
    val commandValue: String?,
    @Enumerated(EnumType.STRING) val status: CommandStatus,
) {
    enum class CommandType {
        REBOOT
    }

    enum class CommandStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESSFUL,
        ERROR,
        CANCELED
    }
}
