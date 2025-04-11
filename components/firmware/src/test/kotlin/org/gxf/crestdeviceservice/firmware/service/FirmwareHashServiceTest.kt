// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readLines
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.junit.jupiter.api.Test

class FirmwareHashServiceTest {
    private val originalFirmwarePacket = Path("src/test/resources/firmware-oneliner.txt").readLines().first()
    private val expectedFirmwarePacket = Path("src/test/resources/expected-firmware-oneliner.txt").readLines().first()
    private val firmwareHashService = FirmwareHashService()

    @Test
    fun shouldGenerateFirstPacket() {
        val firmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "1.10")
        val originalPacket = FirmwarePacket(firmware, 0, originalFirmwarePacket)

        val actualPacket = firmwareHashService.generateDeviceSpecificPacket(originalPacket, "PONMLKJIHGFEDCBA")

        assertThat(actualPacket).isEqualTo(expectedFirmwarePacket)
    }

    @Test
    fun `should not change line when not first or last`() {
        val firmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "1.10")
        val firmwarePacketThatDoesntNeedChanges = "OTA0005-some-random-stuff-halfway-down-the-firmware-file"
        val originalPacket = FirmwarePacket(firmware, 0, firmwarePacketThatDoesntNeedChanges)
        val actualPacket = firmwareHashService.generateDeviceSpecificPacket(originalPacket, "PONMLKJIHGFEDCBA")
        assertThat(actualPacket).isEqualTo(firmwarePacketThatDoesntNeedChanges)
    }
}
