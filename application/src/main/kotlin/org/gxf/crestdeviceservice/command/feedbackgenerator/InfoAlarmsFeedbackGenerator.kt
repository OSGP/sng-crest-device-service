// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmThresholdCalculator
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.springframework.stereotype.Component

@Component
class InfoAlarmsFeedbackGenerator(private val alarmsInfoService: AlarmsInfoService) : CommandFeedbackGenerator {
    val mapper = jacksonObjectMapper()

    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun generateFeedback(message: JsonNode): String {
        val alarmsInfo = alarmsInfoService.getAlarmsInfo(message)
        val calculatedAlarmsInfo = AnalogAlarmThresholdCalculator.calculateThresholdsFromDevice(alarmsInfo)
        return mapper.writeValueAsString(calculatedAlarmsInfo)
    }
}
