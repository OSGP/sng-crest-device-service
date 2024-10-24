// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.springframework.stereotype.Component

@Component
class FirmwareCommandGenerator(
    private val firmwareService: FirmwareService,
) : CommandGenerator {
    override fun generateCommandString(command: Command): String {
        requireNotNull(command.commandValue) { "commandValue should have a firmware name" }
        val deviceId = command.deviceId
        val firmwareName = command.commandValue

        val firmware = firmwareService.findFirmwareByName(firmwareName)
        return firmwareService.getPacketForDevice(firmware, 0, deviceId)
    }

    override fun getSupportedCommand() = Command.CommandType.FIRMWARE
}
