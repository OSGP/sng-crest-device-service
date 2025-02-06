// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmsThresholdTranslator
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class AnalogAlarmThresholdsResultHandler(
    val commandService: CommandService,
    val commandFeedbackService: CommandFeedbackService,
) : CommandResultHandler(commandService, commandFeedbackService) {
    private val partialSuccessUrc = "SET"
    private val partialErrorUrcs = listOf("DLER", "ERR")

    override val supportedCommandType = Command.CommandType.ANALOG_ALARM_THRESHOLDS

    override fun hasSucceeded(command: Command, message: JsonNode): Boolean {
        val channel = getChannelFromCommand(command)
        val fullSuccesUrc = "$channel:$partialSuccessUrc"
        return fullSuccesUrc in message.urcs()
    }

    override fun hasFailed(command: Command, message: JsonNode): Boolean {
        val channel = getChannelFromCommand(command)
        val fullErrorUrcs = partialErrorUrcs.map { "$channel:$it" }
        return message.urcs().any { it in fullErrorUrcs }
    }

    private fun getChannelFromCommand(command: Command): String {
        val commandValue = command.commandValue!!
        val port = commandValue.substringBefore(':')
        return AnalogAlarmsThresholdTranslator.translatePortToChannel(port)
    }
}
