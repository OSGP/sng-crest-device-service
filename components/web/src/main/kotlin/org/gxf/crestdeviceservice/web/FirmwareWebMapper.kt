// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.multipart.MultipartFile

object FirmwareWebMapper {
    private val logger = KotlinLogging.logger {}

    fun mapFirmwareFileToDTO(file: MultipartFile): FirmwareWebDTO {
        val fileContent = String(file.inputStream.readBytes())

        logger.debug { "Contents of firmware file:\n${fileContent}" }

        val packets = fileContent.lines()

        return FirmwareWebDTO(file.originalFilename!!, packets)
    }
}
