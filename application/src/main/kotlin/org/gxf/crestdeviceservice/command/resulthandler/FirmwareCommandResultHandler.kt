// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class FirmwareCommandResultHandler(
    val commandService: CommandService,
    val commandFeedbackService: CommandFeedbackService,
) : CommandResultHandler(commandService, commandFeedbackService) {

    private val successUrc = "OTA:SUC"
    private val errorUrcs = listOf("OTA:CSER", "OTA:HSER", "OTA:RST", "OTA:SWNA", "OTA:FLER")

    override val supportedCommandType = CommandType.FIRMWARE

    override fun hasSucceeded(command: Command, message: JsonNode) = successUrc in message.urcs()

    override fun hasFailed(command: Command, message: JsonNode) = message.urcs().any { it in errorUrcs }
}
