// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.ManyToOne

@Entity
@IdClass(FirmwarePacketCompositeKey::class)
class FirmwarePacket(
    @ManyToOne @Id val firmware: Firmware,
    @Id val packetNumber: Int,
    @Column(length = 1024) // without this, the integration test assumes a length of 255
    val packet: String,
) {
    fun isFirstPacket() = packet.startsWith(OTA_START)

    fun isLastPacket() = packet.endsWith(OTA_DONE)

    companion object {
        const val OTA_START = "OTA0000"
        const val OTA_COMMAND_LENGTH = OTA_START.length
        const val OTA_DONE = ":DONE"
        const val HASH_LENGTH_BYTES = 32
    }
}
