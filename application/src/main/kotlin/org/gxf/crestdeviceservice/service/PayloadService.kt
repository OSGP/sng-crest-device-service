// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.NoMatchingCommandException
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.model.DeviceMessage
import org.gxf.crestdeviceservice.model.Downlink
import org.springframework.stereotype.Service

@Service
class PayloadService(
    private val urcService: UrcService,
    private val firmwareService: FirmwareService,
    private val commandService: CommandService,
) {
    /**
     * Process the payload. This includes
     * - checking URCs and updating the corresponding Commands
     * - checking the payload for FMC (request for new FOTA packet)
     *
     * @param identity The identity of the device
     * @param body The body of the device message
     * @param downlink The downlink to be returned to the device, fill it here if needed
     */
    fun processPayload(identity: String, body: JsonNode, downlink: Downlink) {
        urcService.interpretUrcsInMessage(identity, body)

        addFirmwareCommandIfRequested(DeviceMessage(body), identity, downlink)
    }

    private fun addFirmwareCommandIfRequested(deviceMessage: DeviceMessage, identity: String, downlink: Downlink) {
        val fotaMessageCounter = deviceMessage.getFotaMessageCounter()
        if (fotaMessageCounter > 0) {
            val firmwareCommand =
                commandService.getAllCommandsInProgressForDevice(identity).find {
                    it.type == Command.CommandType.FIRMWARE
                }

            if (firmwareCommand != null) {
                val firmware = firmwareService.findFirmwareByName(firmwareCommand.getRequiredCommandValue())
                val otaCommand = firmwareService.getPacketForDevice(firmware, fotaMessageCounter, identity)
                downlink.addIfPossible(otaCommand)
            } else {
                throw NoMatchingCommandException(
                    "Device $identity requests FOTA packet $fotaMessageCounter (FMC), but no running FIRMWARE command was found")
            }
        }
    }
}
