// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.readLines
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.junit.jupiter.api.Test

class FirmwareHashServiceTest {
    private val logger = KotlinLogging.logger {}
    private val fileContent = Path("src/test/resources/firmware-oneliner.txt").readLines()
    private val firmwareHashService = InternalFirmwareHashService()

    @Test
    fun shouldGenerateFirstPacket() {
        val firmware = Firmware(UUID.randomUUID(), "test-firmware", "", null, emptyList())
        val rawPacket = FirmwarePacket(firmware, 0, fileContent[0])

        val actualPacket = firmwareHashService.generateDeviceSpecificPacket(rawPacket, "PONMLKJIHGFEDCBA")

        logger.info { "Resulting packet: $actualPacket" }
    }
}
