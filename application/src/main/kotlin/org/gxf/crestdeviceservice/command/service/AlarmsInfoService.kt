// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.resulthandler.CommandResultHandler.Companion.downlinks
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.springframework.stereotype.Service

@Service
class AlarmsInfoService {
    private val mapper = jacksonObjectMapper()

    fun getAlarmsInfo(message: JsonNode): AlarmsInfo {
        val downlink = message.downlinks().first { it.contains(Command.CommandType.INFO_ALARMS.downlink) }
        val json = downlink.substringAfter(", ")

        return mapper.readValue<AlarmsInfo>(json)
    }
}
