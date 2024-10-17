// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class FirmwareWebController(val firmwareWebService: FirmwareWebService) {
    private val redirectUrl = "redirect:/web/firmware"

    @GetMapping("/web/firmware")
    fun showUploadForm(model: Model): String {
        return "uploadForm"
    }

    @PostMapping("/web/firmware")
    fun handleFileUpload(@RequestPart("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        if (file.originalFilename.isNullOrEmpty()) {
            redirectAttributes.setMessage("No file provided")
            return redirectUrl
        }

        redirectAttributes.addFlashAttribute("filename", file.originalFilename)

        if (file.isEmpty) {
            redirectAttributes.setMessage("An empty file was provided")
            return redirectUrl
        }
        try {
            val processedPackets = firmwareWebService.processFirmwareFile(file)
            redirectAttributes.setMessage("Successfully processed $processedPackets firmware packets")
        } catch (e: Exception) {
            redirectAttributes.setMessage("Failed to process file: ${e.message}")
        }
        return redirectUrl
    }

    private fun RedirectAttributes.setMessage(message: String) {
        this.addFlashAttribute("message", message)
    }
}
