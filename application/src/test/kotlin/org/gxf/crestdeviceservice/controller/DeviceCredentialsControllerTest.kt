// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import org.gxf.crestdeviceservice.psk.service.PskService
import org.gxf.crestdeviceservice.service.MetricService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PskController::class)
class DeviceCredentialsControllerTest {

    @Autowired private lateinit var mvcRequest: MockMvc

    @MockBean private lateinit var pskService: PskService

    @MockBean private lateinit var metricService: MetricService

    private val url = "https://localhost:9000/psk"

    @Test
    fun shouldReturn404WhenPskForIdentityIsNotFound() {
        val identity = "identity"
        whenever(pskService.getCurrentActiveKey(identity)).thenReturn(null)

        mvcRequest
            .perform(MockMvcRequestBuilders.get(url).header("x-device-identity", identity))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun shouldReturnKeyForIdentity() {
        val identity = "identity"
        whenever(pskService.getCurrentActiveKey(identity)).thenReturn("key")

        mvcRequest
            .perform(MockMvcRequestBuilders.get(url).header("x-device-identity", identity))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("key"))
    }
}
