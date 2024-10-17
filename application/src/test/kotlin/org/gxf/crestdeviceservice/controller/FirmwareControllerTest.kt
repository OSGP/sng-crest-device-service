// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.FirmwareDTOFactory.getFirmwareDTO
import org.gxf.crestdeviceservice.FirmwaresFactory.getFirmwares
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.config.ServerProperties
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.service.FirmwareProducerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FirmwareController::class)
class FirmwareControllerTest {
    @MockBean private lateinit var firmwareService: FirmwareService
    @MockBean private lateinit var firmwareProducerService: FirmwareProducerService
    @MockBean private lateinit var serverProperties: ServerProperties
    @Autowired private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()

    @Test
    fun shouldProcessFirmwareAndSendAllFirmwares() {
        val firmwareDTO = getFirmwareDTO()
        val payload = objectMapper.writeValueAsString(firmwareDTO)
        val firmwares = getFirmwares()
        whenever(firmwareService.processFirmware(eq(firmwareDTO))).thenReturn(firmwares)

        mockMvc
            .perform(post("/web/api/firmware/$FIRMWARE_NAME").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk)

        verify(firmwareService).processFirmware(eq(firmwareDTO))
        verify(firmwareProducerService).send(firmwares)
    }
}
