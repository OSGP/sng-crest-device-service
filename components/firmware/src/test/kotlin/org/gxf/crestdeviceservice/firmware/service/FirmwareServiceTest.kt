// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.firmware.entity.FirmwareFactory
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.gxf.crestdeviceservice.psk.service.DeviceSecretService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class FirmwareServiceTest {
    @InjectMocks private lateinit var firmwareService: FirmwareService
    @Mock private lateinit var firmwareRepository: FirmwareRepository
    @Mock private lateinit var firmwarePacketRepository: FirmwarePacketRepository
    @Mock private lateinit var firmwareHashService: FirmwareHashService
    @Mock private lateinit var deviceSecretService: DeviceSecretService

    @Test
    fun shouldReturnFirmware() {
        val firmwareName = "useThisFirmware.txt"
        val expectedFirmware = FirmwareFactory.getFirmware()
        whenever(firmwareRepository.findByName(firmwareName)).thenReturn(expectedFirmware)

        val actualFirmware = firmwareService.findFirmwareByName(firmwareName)

        assertThat(actualFirmware).isSameAs(expectedFirmware)
    }

    @Test
    fun shouldReturnPacket() {
        val deviceId = "test-device"
        val deviceSecret = "super-duper-hush-hus"
        val packetNumber = 0
        val firmware = FirmwareFactory.getFirmware()
        val packet = FirmwareFactory.getFirmwarePacket(firmware)

        whenever(firmwarePacketRepository.findByFirmwareAndPacketNumber(firmware, packetNumber)).thenReturn(packet)
        whenever(deviceSecretService.getDeviceSecret(deviceId)).thenReturn(deviceSecret)
        whenever(firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret)).thenReturn(packet.packet)

        val actualPacket = firmwareService.getPacketForDevice(firmware, packetNumber, deviceId)

        assertThat(actualPacket).isEqualTo(packet.packet)
    }
}
