// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.service.DeviceMessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sng")
class MessageController(private val deviceMessageService: DeviceMessageService) {
    private val logger = KotlinLogging.logger {}

    /**
     * This endpoint handles incoming crest device messages. Responses are generated synchronously to avoid sending the
     * same downlink twice.
     */
    @PostMapping("/{deviceId}")
    fun incomingDeviceMessage(@PathVariable deviceId: String, @RequestBody message: JsonNode): ResponseEntity<String> {
        logger.debug { "Processing message $message for device $deviceId" }

        return try {
            val downlink = deviceMessageService.processDeviceMessage(message, deviceId)
            logDownlink(downlink, deviceId)
            ResponseEntity.ok(downlink)
        } catch (e: Exception) {
            logger.error(e) {
                "Exception occurred while interpreting message from or creating downlink for device $deviceId"
            }
            ResponseEntity.internalServerError().build()
        } finally {
            logger.debug { "Processed message" }
        }
    }

    private fun logDownlink(downlink: String, deviceId: String) {
        logger.debug {
            if (downlink.contains(Command.CommandType.PSK.name)) {
                // This covers both PSK and PSK:SET, don't log the actual PSK
                "Sending downlink with PSK to device $deviceId"
            } else {
                "Sending downlink '$downlink' to device $deviceId"
            }
        }
    }
}
