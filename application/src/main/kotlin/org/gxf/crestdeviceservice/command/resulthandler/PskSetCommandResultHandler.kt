// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Component

@Component
class PskSetCommandResultHandler(
    val pskService: PskService,
    val commandService: CommandService,
    val commandFeedbackService: CommandFeedbackService,
) : CommandResultHandler(commandService, commandFeedbackService) {

    private val logger = KotlinLogging.logger {}

    private val successUrc = "PSK:SET"
    private val errorUrcs = listOf("PSK:DLER", "PSK:HSER", "PSK:EQER")

    override val supportedCommandType = CommandType.PSK_SET

    override fun hasSucceeded(command: Command, message: JsonNode) = successUrc in message.urcs()

    override fun hasFailed(command: Command, message: JsonNode) = message.urcs().any { it in errorUrcs }

    override fun handleCommandSpecificSuccess(command: Command, message: JsonNode) {
        logger.info { "PSK SET command succeeded: Changing active key for device ${command.deviceId}" }
        pskService.changeActiveKey(command.deviceId)
    }

    override fun handleCommandSpecificFailure(command: Command, message: JsonNode) {
        logger.info { "PSK SET command failed: Setting pending key as invalid for device ${command.deviceId}" }
        pskService.setPendingKeyAsInvalid(command.deviceId)
    }
}
