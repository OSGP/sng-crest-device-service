// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.ManyToOne

@Entity
@IdClass(FirmwarePacketCompositeKey::class)
class FirmwarePacket(@ManyToOne @Id val firmware: Firmware, @Id val packetNumber: Int, val packet: String) {
    fun isFirstPacket() = packet.startsWith(OTA_START)

    fun isLastPacket() = packet.endsWith(OTA_DONE)

    companion object {
        const val OTA_START = "OTA0000"
        const val OTA_DONE = ":DONE"
        const val HASH_LENGTH = 40
    }
}
