// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

object WebControllerHelper {
    fun runAfterFileChecks(
        @RequestPart("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes,
        redirectUrl: String,
        action: () -> Unit,
    ): String {
        if (file.originalFilename.isNullOrEmpty()) {
            redirectAttributes.setMessage("No file provided")
            return redirectUrl
        }

        redirectAttributes.addFlashAttribute("filename", file.originalFilename)

        if (file.isEmpty) {
            redirectAttributes.setMessage("An empty file was provided")
            return redirectUrl
        }

        action()

        return redirectUrl
    }
}

fun RedirectAttributes.setMessage(message: String) {
    this.addFlashAttribute("message", message)
}
