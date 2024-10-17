// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_1
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO

object FirmwareDTOFactory {
    fun getFirmwareDTO() = FirmwareDTO(FIRMWARE_NAME, listOf(FIRMWARE_PACKET_0, FIRMWARE_PACKET_1))
}
