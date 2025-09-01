// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.gxf.crestdeviceservice.service.MetricService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PskController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class PskControllerTest {
    @MockkBean private lateinit var pskService: PskService

    @MockkBean(relaxed = true)
    private lateinit var metricService: MetricService

    @Autowired private lateinit var mockMvc: MockMvc

    private val url = "https://localhost:9000/psk"

    @Test
    fun shouldReturn404WhenPskForIdentityIsNotFound() {
        val identity = "identity"

        every { pskService.getCurrentActiveKey(identity) } throws NoExistingPskException("oops")

        mockMvc //
            .perform(get(url).header("x-device-identity", identity)) //
            .andExpect(status().isNotFound)

        verify(exactly = 1) { metricService.incrementIdentityInvalidCounter() }
    }

    @Test
    fun shouldReturnKeyForIdentity() {
        val identity = "identity"
        every { pskService.getCurrentActiveKey(identity) } returns "key"

        mockMvc //
            .perform(get(url).header("x-device-identity", identity)) //
            .andExpect(status().isOk) //
            .andExpect(content().string("key"))

        verify(exactly = 0) { metricService.incrementIdentityInvalidCounter() }
    }
}
