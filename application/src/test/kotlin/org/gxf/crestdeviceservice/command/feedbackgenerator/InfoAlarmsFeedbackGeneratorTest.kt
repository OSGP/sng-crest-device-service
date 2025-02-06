// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_FEEDBACK
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class InfoAlarmsFeedbackGeneratorTest {
    @MockK private lateinit var alarmsInfoService: AlarmsInfoService

    @InjectMockKs private lateinit var infoAlarmsFeedbackGenerator: InfoAlarmsFeedbackGenerator

    @Test
    fun `should support INFO_ALARMS command type`() {
        val result = infoAlarmsFeedbackGenerator.supportedCommandType

        assertThat(result).isEqualTo(Command.CommandType.INFO_ALARMS)
    }

    @Test
    fun `should generate translated and calculated alarm thresholds feedback`() {
        val downlink = ALARMS_INFO_DOWNLINK
        val message = MessageFactory.messageWithUrc(listOf(), downlink)
        val expected = ALARMS_INFO_FEEDBACK
        val alarmsInfo = ALARMS_INFO
        every { alarmsInfoService.getAlarmsInfo(any()) } returns alarmsInfo

        val result = infoAlarmsFeedbackGenerator.generateFeedback(message)

        assertThat(result).isEqualTo(expected)
    }
}
