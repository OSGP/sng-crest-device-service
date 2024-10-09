// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import java.util.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FirmwarePacketTest {
    @ParameterizedTest
    @CsvSource(
        "OTA0000......, true, false",
        "OTA0001....., false, false",
        "OTA0000.....:DONE, true, true",
        "OTA1234....:DONE, false, true",
        "OTA3333...OTA0000...:DONE..., false, false",
    )
    fun shouldReportCorrectForm(firmwarePacket: String, expectedFirstPacket: Boolean, expectedLastPacket: Boolean) {
        val firmware = Firmware(UUID.randomUUID(), "test", "some-hash")
        val packet = FirmwarePacket(firmware, 0, firmwarePacket)

        assertThat(packet.isFirstPacket()).isEqualTo(expectedFirstPacket)
        assertThat(packet.isLastPacket()).isEqualTo(expectedLastPacket)
    }
}
