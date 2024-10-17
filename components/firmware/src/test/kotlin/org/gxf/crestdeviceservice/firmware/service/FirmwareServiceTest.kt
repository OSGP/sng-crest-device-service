// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareDTO
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_1
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock

@ExtendWith(MockitoExtension::class)
class FirmwareServiceTest {
    private val firmwareRepository = mock<FirmwareRepository>()
    private val firmwarePacketRepository = mock<FirmwarePacketRepository>()
    private val firmwareMapper = mock<FirmwareMapper>()
    private val firmwareService = FirmwareService(firmwareRepository, firmwarePacketRepository, firmwareMapper)

    @BeforeEach
    fun setup() {
        firmwareRepository.deleteAll()
        firmwarePacketRepository.deleteAll()
        val previousFirmware = getPreviousFirmwareEntity()
        firmwareRepository.save(previousFirmware)
        firmwarePacketRepository.save(FirmwarePacket(previousFirmware, 0, FIRMWARE_PACKET_0))
        firmwarePacketRepository.save(FirmwarePacket(previousFirmware, 1, FIRMWARE_PACKET_1))
    }

    @Test
    fun processFirmware() {
        val firmwareDTO = getFirmwareDTO()

        val result = firmwareService.processFirmware(firmwareDTO)

        assertThat(result.firmwares.size).isEqualTo(2)
        assertThat(firmwareRepository.findByName(FIRMWARE_NAME)).isEqualTo(firmwareDTO)
        assertThat(firmwarePacketRepository.findAll().size).isEqualTo(4)
    }
}
