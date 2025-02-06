// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class AlarmsInfoServiceTest {
    @InjectMockKs lateinit var alarmsInfoService: AlarmsInfoService

    @Test
    fun getAlarmsInfo() {
        val message = MessageFactory.messageWithUrc(listOf(), ALARMS_INFO_DOWNLINK)
        val expected = ALARMS_INFO

        val result = alarmsInfoService.getAlarmsInfo(message)

        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("incorrectDownlinks")
    fun getAlarmsInfoThrowsException(downlink: String) {
        val message = MessageFactory.messageWithUrc(listOf(), downlink)

        assertThatThrownBy { alarmsInfoService.getAlarmsInfo(message) }.isInstanceOf(Exception::class.java)
    }

    companion object {
        @JvmStatic
        fun incorrectDownlinks(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "\"!INFO:ALARMS\", {\"AL\":[0,1,0,1,0], \"AL1\":[0,0,0,0,0], \"AL6\":[0,500,1000,1500,10]}"
                ),
                Arguments.of("{\"AL0\":[0,1,0,1,0], \"AL1\":[0,0,0,0,0], \"AL6\":[0,500,1000,1500,10]}"),
                Arguments.of("\"!INFO:ALARMS\""),
                Arguments.of("\"!INFO:ALARMS\", {\"AL0\":0,1,0,1,0, \"AL1\":0,0,0,0,0, \"AL6\":0,500,1000,1500,10}"),
                Arguments.of("\"!INFO:ALARMS\", \"AL0\":[0,1,0,1,0], \"AL1\":[0,0,0,0,0], \"AL6\":[0,500,1000,1500,10]"),
            )
    }
}
