// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.model.DeviceMessage
import org.gxf.crestdeviceservice.model.Downlink
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PayloadServiceTest {
    @InjectMocks private lateinit var payloadService: PayloadService

    @Mock private lateinit var urcService: UrcService
    @Mock private lateinit var firmwareService: FirmwareService
    @Mock private lateinit var commandService: CommandService
    @Mock private lateinit var commandFeedbackService: CommandFeedbackService

    private val mapper = ObjectMapper()

    @Test
    fun shouldProcessUrcs() {
        val message = TestHelper.messageTemplate()
        val downlink = Downlink()
        val deviceId = "device-id"

        payloadService.processPayload(deviceId, message, downlink)

        verify(urcService, times(1)).interpretUrcsInMessage(deviceId, message)
    }

    @Test
    fun shouldSupplyOtaCommandForFmc() {
        val packetNumber = 3
        val packetCount = 20
        val message = TestHelper.messageTemplate()
        message.set<JsonNode>(DeviceMessage.FMC_FIELD, mapper.readTree(packetNumber.toString()))
        val downlink = Downlink()
        val deviceId = "device-id"
        val firmwareCommand = CommandFactory.firmwareCommandInProgress()
        val firmwareName = firmwareCommand.commandValue!!
        val firmware = Firmware(UUID.randomUUID(), firmwareName, "some-hash", null)
        val otaCommand = "OTA0003ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        whenever(commandService.getAllCommandsInProgressForDevice(deviceId)).thenReturn(listOf(firmwareCommand))
        whenever(firmwareService.findFirmwareByName(firmwareName)).thenReturn(firmware)
        whenever(firmwareService.getPacketForDevice(firmware, packetNumber, deviceId)).thenReturn(otaCommand)
        whenever(firmwareService.countFirmwarePacketsByName(firmwareName)).thenReturn(packetCount)

        payloadService.processPayload(deviceId, message, downlink)

        verify(urcService, times(1)).interpretUrcsInMessage(deviceId, message)
        assertThat(downlink.getDownlink()).contains(otaCommand)

        verify(commandFeedbackService).sendProgressFeedback(packetNumber + 1, packetCount, firmwareCommand)
    }
}
