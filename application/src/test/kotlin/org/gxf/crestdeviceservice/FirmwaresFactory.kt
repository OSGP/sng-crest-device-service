// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.Firmware
import com.alliander.sng.FirmwareType
import com.alliander.sng.Firmwares
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.TestConstants.NUMBER_OF_PACKETS

object FirmwaresFactory {
    fun getFirmwares() = Firmwares.newBuilder().setFirmwares(listOf(firmware())).build()

    private fun firmware() =
        Firmware.newBuilder()
            .setName(FIRMWARE_NAME)
            .setType(FirmwareType.device)
            .setVersion(FIRMWARE_VERSION)
            .setFromVersion(FIRMWARE_FROM_VERSION)
            .setNumberOfPackages(NUMBER_OF_PACKETS)
            .build()
}
