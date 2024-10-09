// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.gxf.crestdeviceservice.service.MetricService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PskController::class)
class PskControllerTest {

    @Autowired private lateinit var mvcRequest: MockMvc

    @MockBean private lateinit var pskService: PskService

    @MockBean private lateinit var metricService: MetricService

    @Test
    fun shouldReturn404WhenPskForIdentityIsNotFound() {
        val identity = "identity"
        whenever(pskService.getCurrentActiveKey(identity)).then { throw NoExistingPskException("oops") }

        mvcRequest
            .perform(MockMvcRequestBuilders.get("/psk").header("x-device-identity", identity))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
        verify(metricService, times(1)).incrementIdentityInvalidCounter()
    }

    @Test
    fun shouldReturnKeyForIdentity() {
        val identity = "identity"
        whenever(pskService.getCurrentActiveKey(identity)).thenReturn("key")

        mvcRequest
            .perform(MockMvcRequestBuilders.get("/psk").header("x-device-identity", identity))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("key"))
        verify(metricService, never()).incrementIdentityInvalidCounter()
    }
}
