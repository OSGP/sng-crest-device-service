// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareWebDTOFactory.getFirmwareWebDTO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

@AutoConfigureMockRestServiceServer
@ExtendWith(MockitoExtension::class)
class HttpClientTest {
    private val restClientBuilder = RestClient.builder()
    private val httpClient = HttpClient(restClientBuilder)
    private val mapper = ObjectMapper()

    @Test
    fun postFirmware() {
        val firmwareWebDTO = getFirmwareWebDTO()
        val firmwareJson = mapper.writeValueAsString(firmwareWebDTO)
        val uri = HttpClient.FIRMWARE_API.plus("/${firmwareWebDTO.name}")
        val mockServer = MockRestServiceServer.bindTo(restClientBuilder).build()

        mockServer
            .expect(requestTo(uri))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json(firmwareJson))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andRespond(withSuccess())

        val response = httpClient.postFirmware(getFirmwareWebDTO())

        mockServer.verify()
        assertThat(response.statusCode.is2xxSuccessful)
    }
}
