// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.NoMatchingCommandException
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandResultService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.model.DeviceMessage
import org.gxf.crestdeviceservice.model.Downlink
import org.springframework.stereotype.Service

@Service
class PayloadService(
    private val commandResultService: CommandResultService,
    private val firmwareService: FirmwareService,
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService,
) {
    /**
     * Process the payload. This includes
     * - checking the payload for results for the commands sent via downlinks
     * - checking the payload for FMC (request for new FOTA packet)
     *
     * @param deviceId The identity of the device
     * @param body The body of the device message
     * @param downlink The downlink to be returned to the device, fill it here if needed
     */
    fun processPayload(deviceId: String, body: JsonNode, downlink: Downlink) {
        commandResultService.handleMessage(deviceId, body)

        val deviceMessage = DeviceMessage(body)

        addFirmwareCommandIfRequested(deviceMessage, deviceId, downlink)?.let { firmware ->
            sendProgress(deviceMessage, firmware)
        }
    }

    private fun addFirmwareCommandIfRequested(
        deviceMessage: DeviceMessage,
        identity: String,
        downlink: Downlink,
    ): Command? =
        deviceMessage
            .getFotaMessageCounter()
            .takeIf { it > 0 }
            ?.let { fotaMessageCounter ->
                val firmwareCommand =
                    commandService.getAllCommandsInProgressForDevice(identity).find {
                        it.type == Command.CommandType.FIRMWARE
                    }

                if (firmwareCommand != null) {
                    val firmware = firmwareService.findFirmwareByName(firmwareCommand.getRequiredCommandValue())
                    val otaCommand = firmwareService.getPacketForDevice(firmware, fotaMessageCounter, identity)
                    downlink.addIfPossible(otaCommand)

                    firmwareCommand
                } else {
                    throw NoMatchingCommandException(
                        "Device $identity requests FOTA packet $fotaMessageCounter (FMC), but no running FIRMWARE command was found"
                    )
                }
            }

    private fun sendProgress(deviceMessage: DeviceMessage, firmwareCommand: Command) {
        val packet = deviceMessage.getFotaMessageCounter() + 1
        val packetCount = firmwareService.countFirmwarePacketsByName(firmwareCommand.getRequiredCommandValue())

        commandFeedbackService.sendProgressFeedback(packet, packetCount, firmwareCommand)
    }
}
