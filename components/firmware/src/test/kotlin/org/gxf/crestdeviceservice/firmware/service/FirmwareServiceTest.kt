// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwares
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
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
    private val firmwareMapper = mock<FirmwareMapper>()
    private val firmwareService = FirmwareService(firmwareRepository, firmwareMapper)

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
}
