// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.service.ShipmentFileService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller()
@RequestMapping("/web/shipmentfile")
class ShipmentFileController(private val shipmentFileService: ShipmentFileService) {
    private val logger = KotlinLogging.logger {}
    private val redirectUrl = "redirect:/web/shipmentfile"

    @GetMapping fun showUploadForm() = "shipmentFileUploadForm"

    @PostMapping
    fun handleFileUpload(@RequestPart("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String =
        WebControllerHelper.runAfterFileChecks(file, redirectAttributes, redirectUrl) {
            try {
                logger.info { "Processing shipment file with name: ${file.originalFilename}" }

                val processedDevices = shipmentFileService.processShipmentFile(file)

                logger.info { "Shipment file successfully processed" }
                redirectAttributes.setMessage("Successfully processed $processedDevices devices")
            } catch (e: Exception) {
                logger.error(e) { "Failed to process firmware file" }
                when (e) {
                    is JsonParseException,
                    is JsonMappingException,
                        -> redirectAttributes.setMessage("Failed to parse file as JSON")
                    else -> redirectAttributes.setMessage("Failed to process file")
                }
            }
        }
}
