// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import com.alliander.sng.FirmwareType
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareDTO
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_1
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_WRONG_NAME
import org.gxf.crestdeviceservice.TestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FirmwareMapperTest {
    private val firmwareRepository = mock<FirmwareRepository>()
    private val firmwareMapper = FirmwareMapper(firmwareRepository)

    @Test
    fun mapFirmwareDTOToEntity() {
        val firmwareDTO = getFirmwareDTO()
        val previousFirmware = getPreviousFirmwareEntity()
        whenever(firmwareRepository.findByVersion(FIRMWARE_FROM_VERSION)).thenReturn(previousFirmware)

        val result = firmwareMapper.mapFirmwareDTOToEntity(firmwareDTO)

        assertThat(result.name).isEqualTo(FIRMWARE_NAME)
        assertThat(result.version).isEqualTo(FIRMWARE_VERSION)
        assertThat(result.previousFirmwareId).isEqualTo(PREVIOUS_FIRMWARE_UUID)
        result.packets.forEach { packet -> assertThat(packet.firmware).isEqualTo(result) }
        assertThat(result.packets[0].packetNumber).isEqualTo(0)
        assertThat(result.packets[1].packetNumber).isEqualTo(1)
        assertThat(result.packets[0].packet).isEqualTo(FIRMWARE_PACKET_0)
        assertThat(result.packets[1].packet).isEqualTo(FIRMWARE_PACKET_1)
    }

    @Test
    fun mapFirmwareDTOToEntityThrowsException() {
        val firmwareDTO = getFirmwareDTO()

        assertThrows<FirmwareException> { firmwareMapper.mapFirmwareDTOToEntity(firmwareDTO) }
    }

    @Test
    fun mapEntitiesToFirmwares() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_NAME))
        val previousFirmware = getPreviousFirmwareEntity()
        whenever(firmwareRepository.findById(PREVIOUS_FIRMWARE_UUID)).thenReturn(Optional.of(previousFirmware))

        val result = firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)

        val firmware = result.firmwares.first()
        assertThat(firmware.name).isEqualTo(FIRMWARE_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_VERSION)
        assertThat(firmware.type).isEqualTo(FirmwareType.device)
        assertThat(firmware.fromVersion).isEqualTo(FIRMWARE_FROM_VERSION)
        assertThat(firmware.numberOfPackages).isEqualTo(1)
    }

    @Test
    fun mapEntitiesToFirmwaresThrowsException() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_WRONG_NAME))

        assertThrows<FirmwareException> { firmwareMapper.mapEntitiesToFirmwares(firmwareEntities) }
    }
}
