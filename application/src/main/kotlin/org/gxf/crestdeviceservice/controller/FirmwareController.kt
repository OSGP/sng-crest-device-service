// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.service.FirmwareProducerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/web/api/firmware")
class FirmwareController(val firmwareService: FirmwareService, val firmwareProducerService: FirmwareProducerService) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/{name}")
    fun post(@NonNull @PathVariable name: String, @NonNull @RequestBody firmware: FirmwareDTO): ResponseEntity<String> {
        try {
            logger.debug { "Processing firmware file with name $name" }
            val firmwares = firmwareService.processFirmware(firmware)
            logger.info { "Processed firmware file" }
            firmwareProducerService.send(firmwares)
            logger.info { "Sent updated list of firmwares to Maki" }
            return ResponseEntity.ok("Firmware successfully processed")
        } catch (e: FirmwareException) {
            logger.error(e) { "Failed to process firmware file" }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }
}
