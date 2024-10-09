// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BaseJsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestConstants
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UrcServiceTest {
    private val pskService = mock<PskService>()
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val urcService = UrcService(pskService, commandService, commandFeedbackService)
    private val mapper = spy<ObjectMapper>()

    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
        private const val PSK_DOWNLINK =
            "!PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3;PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3:SET"
        private const val REBOOT_DOWNLINK = "!CMD:REBOOT"
        private const val DEVICE_ID = TestConstants.DEVICE_ID

        @JvmStatic
        private fun containingPskErrorUrcs() =
            Stream.of(
                listOf("PSK:DLER"), listOf("PSK:HSER"), listOf("TS:ERR", "PSK:DLER"), listOf("PSK:DLER", "PSK:EQER"))

        @JvmStatic
        private fun notContainingPskUrcs() =
            Stream.of(
                listOf("INIT"),
                listOf("ENPD"),
                listOf("TEL:RBT"),
                listOf("JTR"),
                listOf("WDR"),
                listOf("BOR"),
                listOf("EXR"),
                listOf("POR"),
                listOf("INIT", "BOR", "POR"))
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val urcs = listOf("PSK:TMP", "PSK:SET")
        val pskCommandInProgress = CommandFactory.pskCommandInProgress()
        val pskSetCommandInProgress = CommandFactory.pskSetCommandInProgress()
        val pskCommandsInProgress = listOf(pskCommandInProgress, pskSetCommandInProgress)
        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)
        whenever(pskService.isPendingPskPresent(DEVICE_ID)).thenReturn(true)
        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID)).thenReturn(pskCommandsInProgress)
        whenever(commandService.save(pskCommandInProgress)).thenReturn(pskCommandInProgress)
        whenever(commandService.save(pskSetCommandInProgress)).thenReturn(pskSetCommandInProgress)

        urcService.interpretURCsInMessage(DEVICE_ID, message)

        verify(pskService).changeActiveKey(DEVICE_ID)
        assertThat(pskCommandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        assertThat(pskSetCommandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
    }

    @ParameterizedTest(name = "should set pending key as invalid for {0}")
    @MethodSource("containingPskErrorUrcs")
    fun shouldSetPendingKeyAsInvalidWhenPskFailureURCReceived(urcs: List<String>) {
        val pskCommandInProgress = CommandFactory.pskCommandInProgress()
        val pskSetCommandInProgress = CommandFactory.pskSetCommandInProgress()
        whenever(pskService.isPendingPskPresent(DEVICE_ID)).thenReturn(true)
        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID))
            .thenReturn(listOf(pskCommandInProgress, pskSetCommandInProgress))
        whenever(commandService.save(pskCommandInProgress)).thenReturn(pskCommandInProgress)
        whenever(commandService.save(pskSetCommandInProgress)).thenReturn(pskSetCommandInProgress)
        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        urcService.interpretURCsInMessage(DEVICE_ID, message)

        verify(pskService).setPendingKeyAsInvalid(DEVICE_ID)
        verify(commandService, times(2)).save(any<Command>())
        verify(commandFeedbackService, times(2)).sendFeedback(any<CommandFeedback>())
        assertThat(pskCommandInProgress.status).isEqualTo(Command.CommandStatus.ERROR)
        assertThat(pskSetCommandInProgress.status).isEqualTo(Command.CommandStatus.ERROR)
    }

    @ParameterizedTest(name = "should not set pending key as invalid for {0}")
    @MethodSource("notContainingPskUrcs")
    fun shouldNotSetPendingKeyAsInvalidWhenOtherURCReceived(urcs: List<String>) {
        val pskCommands = CommandFactory.pskCommandsInProgress()
        whenever(pskService.isPendingPskPresent(DEVICE_ID)).thenReturn(true)
        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID)).thenReturn(pskCommands)
        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        urcService.interpretURCsInMessage(DEVICE_ID, message)

        verify(pskService, times(0)).setPendingKeyAsInvalid(DEVICE_ID)
    }

    @Test
    fun handleSuccessUrcForRebootCommand() {
        val urcs = listOf("INIT", "WDR")
        val commandInProgress = CommandFactory.rebootCommandInProgress()
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        whenever(pskService.isPendingPskPresent(DEVICE_ID)).thenReturn(false)
        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID)).thenReturn(listOf(commandInProgress))
        whenever(commandService.save(commandInProgress)).thenReturn(commandInProgress)

        urcService.interpretURCsInMessage(DEVICE_ID, message)

        val expectedCommandFeedback =
            CommandFeedbackMapper.commandEntityToCommandFeedback(
                commandInProgress, CommandStatus.Successful, "Command handled successfully")

        verify(commandService).save(commandInProgress)
        verify(commandFeedbackService).sendFeedback(refEq(expectedCommandFeedback, "timestampStatus"))
        assertThat(commandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
    }

    @Test
    fun shouldDoNothingIfUrcDoesNotConcernCommandInProgress() {
        val urcs = listOf("ENPD")
        val commandInProgress = CommandFactory.rebootCommandInProgress()
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        whenever(pskService.isPendingPskPresent(DEVICE_ID)).thenReturn(false)
        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID)).thenReturn(listOf(commandInProgress))

        urcService.interpretURCsInMessage(DEVICE_ID, message)

        verify(commandService, times(0)).save(any<List<Command>>())
        verify(commandFeedbackService, times(0)).sendFeedback(any<CommandFeedback>())
    }

    @ParameterizedTest(name = "should do nothing when downlink is {0}")
    @ValueSource(strings = ["0", "", " ", "\n"])
    fun shouldDoNothingWhenDownlinkIsBlank(downlink: String) {
        val urcs = listOf("INIT")
        val message = updateUrcInMessage(urcs, downlink)

        whenever(commandService.getAllCommandsInProgressForDevice(DEVICE_ID)).thenReturn(listOf())

        urcService.interpretURCsInMessage(DEVICE_ID, message)
    }

    private fun updateUrcInMessage(urcs: List<String>, downlink: String): JsonNode {
        val message = TestHelper.messageTemplate()
        val urcFieldValue = urcFieldValue(urcs, downlink)

        message.replace(URC_FIELD, urcFieldValue)
        return message
    }

    private fun urcFieldValue(urcs: List<String>, downlink: String): ArrayNode? {
        val urcNodes = urcs.map { urc -> TextNode(urc) }
        val downlinkNode = ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(downlink)))
        val urcsPlusReceivedDownlink: MutableList<BaseJsonNode> = mutableListOf()
        urcsPlusReceivedDownlink.addAll(urcNodes)
        urcsPlusReceivedDownlink.add(downlinkNode)
        val urcFieldValue = mapper.valueToTree<ArrayNode>(urcsPlusReceivedDownlink)
        return urcFieldValue
    }
}
