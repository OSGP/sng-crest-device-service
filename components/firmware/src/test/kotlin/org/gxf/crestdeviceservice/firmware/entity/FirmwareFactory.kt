// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import java.util.*

object FirmwareFactory {
    fun getFirmware() = Firmware(id = UUID.randomUUID(), name = "A Firmware", hash = "some-hash")

    fun getFirmwarePacket(firmware: Firmware = getFirmware()) =
        FirmwarePacket(firmware = firmware, packetNumber = 0, packet = "OTA0000...more.important.stuff")
}
