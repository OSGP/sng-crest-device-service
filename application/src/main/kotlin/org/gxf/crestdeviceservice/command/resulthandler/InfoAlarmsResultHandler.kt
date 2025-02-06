// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class InfoAlarmsResultHandler(
    commandService: CommandService,
    commandFeedbackService: CommandFeedbackService,
    private val alarmsInfoService: AlarmsInfoService,
) : CommandResultHandler(commandService, commandFeedbackService) {
    private val errorUrcs = listOf("INFO:DLER", "INFO:ERR", "INFO:DLNA")

    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun hasSucceeded(command: Command, message: JsonNode) =
        try {
            alarmsInfoService.getAlarmsInfo(message)
            true
        } catch (_: Exception) {
            false
        }

    override fun hasFailed(command: Command, message: JsonNode): Boolean = message.urcs().any { it in errorUrcs }
}
