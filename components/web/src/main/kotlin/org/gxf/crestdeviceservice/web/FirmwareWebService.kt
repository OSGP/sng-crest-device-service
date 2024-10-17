// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.http.HttpClient
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FirmwareWebService(val httpClient: HttpClient) {
    private val logger = KotlinLogging.logger {}

    fun processFirmwareFile(file: MultipartFile): Int {
        logger.info { "Processing file: ${file.originalFilename}" }
        val firmwareFileDTO = FirmwareWebMapper.mapFirmwareFileToDTO(file)
        logger.info { "Processing ${firmwareFileDTO.packets.size} packets from firmware file" }

        httpClient.postFirmware(firmwareFileDTO)

        return firmwareFileDTO.packets.size
    }
}
