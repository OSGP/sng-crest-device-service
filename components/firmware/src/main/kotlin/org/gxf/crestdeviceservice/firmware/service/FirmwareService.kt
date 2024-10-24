// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.gxf.crestdeviceservice.psk.service.DeviceSecretService
import org.springframework.stereotype.Service

@Service
class FirmwareService(
    private val firmwareRepository: FirmwareRepository,
    private val firmwarePacketRepository: FirmwarePacketRepository,
    private val firmwareHashService: FirmwareHashService,
    private val deviceSecretService: DeviceSecretService,
) {
    fun findFirmwareByName(name: String) = firmwareRepository.findByName(name)

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
        val deviceSecret = deviceSecretService.getDeviceSecret(deviceId)
        return firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret)
    }
}
