// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import com.alliander.sng.Firmwares
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FirmwareService(
    private val firmwareRepository: FirmwareRepository,
    private val firmwarePacketRepository: FirmwarePacketRepository,
    private val firmwareMapper: FirmwareMapper,
    private val firmwareHashService: FirmwareHashService,
    private val deviceService: DeviceService,
) {
    private val logger = KotlinLogging.logger {}

    fun processFirmware(file: MultipartFile): Firmwares {
        val firmware = firmwareMapper.mapFirmwareFileToEntity(file)
        if (firmwareRepository.findByName(firmware.name) != null) {
            throw FirmwareException("Firmware with name ${firmware.name} already exists")
        }
        save(firmware)
        val firmwareEntities: List<Firmware> = firmwareRepository.findAll()
        return firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)
    }

    private fun save(firmware: Firmware) {
        logger.info { "Saving firmware with name ${firmware.name} to database" }
        firmwareRepository.save(firmware)
    }

    fun findFirmwareByName(name: String) =
        checkNotNull(firmwareRepository.findByName(name)) { "Firmware with name $name not found" }

    fun countFirmwarePacketsByName(name: String) = findFirmwareByName(name).packets.size

    /**
     * Gets a ready-to-go firmware packet for a device. If required, the firmware hashes in the packet are replaced with
     * device-specific hashes for validation.
     *
     * @param firmware the firmware from which to get the packet
     * @param packetNr the sequence number of the packet. *This is zero-based (following the supplier specs)*
     * @param deviceId ID of the receiving device, needed to create device-specific hashes if required
     * @return Downlink command ready to be sent to the device
     */
    fun getPacketForDevice(firmware: Firmware, packetNr: Int, deviceId: String): String {
        val packet = firmwarePacketRepository.findByFirmwareAndPacketNumber(firmware, packetNr)
        val deviceSecret = deviceService.getDevice(deviceId).secret
        return firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret)
    }
}
