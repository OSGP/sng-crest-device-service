// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwares
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class FirmwareServiceTest {
    private val firmwareRepository = mock<FirmwareRepository>()
    private val firmwarePacketRepository = mock<FirmwarePacketRepository>()
    private val firmwareMapper = mock<FirmwareMapper>()
    private val firmwareHashService = mock<FirmwareHashService>()
    private val deviceService = mock<DeviceService>()
    private val firmwareService =
        FirmwareService(
            firmwareRepository,
            firmwarePacketRepository,
            firmwareMapper,
            firmwareHashService,
            deviceService
        )

    @Test
    fun processFirmware() {
        val firmwareFile = FirmwareFactory.getFirmwareFile()
        val firmwareEntity = getFirmwareEntity(firmwareFile.name)
        val previousFirmware = getPreviousFirmwareEntity()
        val allFirmwareEntities = listOf(previousFirmware, firmwareEntity)
        val firmwares = getFirmwares()
        whenever(firmwareMapper.mapFirmwareFileToEntity(firmwareFile)).thenReturn(firmwareEntity)
        whenever(firmwareRepository.findAll()).thenReturn(allFirmwareEntities)
        whenever(firmwareMapper.mapEntitiesToFirmwares(listOf(previousFirmware, firmwareEntity))).thenReturn(firmwares)

        val result = firmwareService.processFirmware(firmwareFile)

        verify(firmwareRepository).save(firmwareEntity)
        assertThat(result).isEqualTo(firmwares)
    }

    @Test
    fun shouldReturnFirmware() {
        val firmwareName = "useThisFirmware.txt"
        val expectedFirmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "3")
        whenever(firmwareRepository.findByName(firmwareName)).thenReturn(expectedFirmware)

        val actualFirmware = firmwareService.findFirmwareByName(firmwareName)

        assertThat(actualFirmware).isSameAs(expectedFirmware)
    }

    @Test
    fun shouldCountPackets() {
        val firmwareName = "firmware"
        val packetCount = 10

        val firmware = Firmware(id = UUID.randomUUID(), name = firmwareName, version = "1", packets = mutableListOf())

        repeat(packetCount) { packetNumber ->
            firmware.packets += FirmwarePacket(firmware, packetNumber, "packet $packetNumber")
        }

        whenever(firmwareRepository.findByName(firmwareName)).thenReturn(firmware)

        assertThat(firmwareService.countFirmwarePacketsByName(firmwareName)).isEqualTo(packetCount)
    }

    @Test
    fun shouldReturnPacket() {
        val deviceId = "test-device"
        val deviceSecret = "super-duper-hush-hush"
        val device = Device("id", deviceSecret)
        val packetNumber = 0
        val firmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "3")
        val packet = FirmwarePacket(firmware, 0, "the-packet-contents")

        whenever(firmwarePacketRepository.findByFirmwareAndPacketNumber(firmware, packetNumber)).thenReturn(packet)
        whenever(deviceService.getDevice(deviceId)).thenReturn(device)
        whenever(firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret)).thenReturn(packet.packet)

        val actualPacket = firmwareService.getPacketForDevice(firmware, packetNumber, deviceId)

        assertThat(actualPacket).isEqualTo(packet.packet)
    }
}
