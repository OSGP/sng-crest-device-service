// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.service.FirmwareProducerService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/web/firmware")
class FirmwareController(val firmwareService: FirmwareService, val firmwareProducerService: FirmwareProducerService) {
    private val logger = KotlinLogging.logger {}
    private val redirectUrl = "redirect:/web/firmware"

    @GetMapping fun showUploadForm() = "firmwareUploadForm"

    @PostMapping
    fun handleFileUpload(@RequestPart("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String =
        WebControllerHelper.runAfterFileChecks(file, redirectAttributes, redirectUrl) {
            try {
                logger.info { "Processing firmware file with name: ${file.originalFilename}" }

                val savedFirmware = firmwareService.processFirmware(file)
                firmwareProducerService.sendAllFirmwares()

                logger.info { "Firmware file successfully processed" }
                redirectAttributes.setMessage("Successfully processed ${savedFirmware.packets.size} firmware packets")
            } catch (exception: FirmwareException) {
                logger.error(exception) { "Failed to process firmware file" }
                redirectAttributes.setMessage("Failed to process file: ${exception.message}")
            }
        }
}
