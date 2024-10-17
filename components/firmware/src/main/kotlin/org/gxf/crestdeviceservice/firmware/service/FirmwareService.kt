// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import com.alliander.sng.Firmwares
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Service

@Service
class FirmwareService(
    private val firmwareRepository: FirmwareRepository,
    private val firmwarePacketRepository: FirmwarePacketRepository,
    private val firmwareMapper: FirmwareMapper,
) {
    private val logger = KotlinLogging.logger {}

    fun processFirmware(firmwareDTO: FirmwareDTO): Firmwares {
        val firmware = firmwareMapper.mapFirmwareDTOToEntity(firmwareDTO)
        save(firmware)
        val firmwareEntities: List<Firmware> = firmwareRepository.findAll()
        return firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)
    }

    private fun save(firmware: Firmware) {
        logger.info { "Saving firmware with name ${firmware.name} to database" }
        firmwareRepository.save(firmware)
        firmware.packets.forEach { packet -> firmwarePacketRepository.save(packet) }
    }
}
