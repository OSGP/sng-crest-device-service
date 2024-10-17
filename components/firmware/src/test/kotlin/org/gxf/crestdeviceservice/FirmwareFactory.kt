// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.util.UUID
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_1
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_UUID
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.TestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket

object FirmwareFactory {
    fun getFirmwareDTO() = FirmwareDTO(FIRMWARE_NAME, listOf(FIRMWARE_PACKET_0, FIRMWARE_PACKET_1))

    fun getFirmwareEntity(name: String): Firmware {
        val firmware = Firmware(FIRMWARE_UUID, name, FIRMWARE_VERSION, PREVIOUS_FIRMWARE_UUID, mutableListOf())
        val packet = getFirmwarePacket(firmware)
        firmware.packets.add(packet)
        return firmware
    }

    fun getPreviousFirmwareEntity(): Firmware =
        Firmware(PREVIOUS_FIRMWARE_UUID, FIRMWARE_NAME, FIRMWARE_FROM_VERSION, UUID.randomUUID(), mutableListOf())

    fun getFirmwarePacket(firmware: Firmware) = FirmwarePacket(firmware, 0, FIRMWARE_PACKET_0)
}
