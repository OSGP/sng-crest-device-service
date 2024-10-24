// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RspController::class)
class RspControllerTest {
    @MockBean private lateinit var commandService: CommandService

    @Autowired private lateinit var mockMvc: MockMvc

    @Captor private lateinit var commandCaptor: ArgumentCaptor<Command>

    @Test
    fun `should save RSP command`() {
        mockMvc //
            .perform(post("/test/{deviceId}/rsp", DEVICE_ID)) //
            .andExpect(status().isOk)

        verify(commandService).saveCommand(capture(commandCaptor))

        val command = commandCaptor.value
        assertThat(command.deviceId).isEqualTo(DEVICE_ID)
        assertThat(command.status).isEqualTo(Command.CommandStatus.PENDING)
        assertThat(command.type).isEqualTo(Command.CommandType.RSP)
    }

    @Test
    fun `should save RSP2 command`() {
        mockMvc //
            .perform(post("/test/{deviceId}/rsp2", DEVICE_ID)) //
            .andExpect(status().isOk)

        verify(commandService).saveCommand(capture(commandCaptor))

        val command = commandCaptor.value
        assertThat(command.deviceId).isEqualTo(DEVICE_ID)
        assertThat(command.status).isEqualTo(Command.CommandStatus.PENDING)
        assertThat(command.type).isEqualTo(Command.CommandType.RSP2)
    }
}
