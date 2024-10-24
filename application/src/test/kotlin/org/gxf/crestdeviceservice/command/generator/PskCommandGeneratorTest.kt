// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PskCommandGeneratorTest {
    private val pskService = mock<PskService>()
    private val generator = PskCommandGenerator(pskService)

    @ParameterizedTest
    @CsvSource(
        "1234567890123456,ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071,secret",
        "1234567890123456,78383f73855e7595f8d31ee7cabdf854bc4e70d036f225f8d144d566083c7d01,different-secret",
        "6543210987654321,5e15cf0f8a55b58a54f51dda17c1d1645ebc145f912888ec2e02a55d7b7baea4,secret",
        "6543210987654321,64904d94590a354cecd8e65630289bcc22103c07b08c009b0b12a8ef0d58af9d,different-secret")
    fun shouldCreateACorrectPskSetCommandWithHash(key: String, expectedHash: String, usedSecret: String) {
        val pskCommandPending = CommandFactory.pendingPskCommand()
        val preSharedKey = PreSharedKey("identity", 0, Instant.now(), key, usedSecret, PreSharedKeyStatus.PENDING)
        whenever(pskService.getCurrentReadyPsk(pskCommandPending.deviceId)).thenReturn(preSharedKey)

        val result = generator.generateCommandString(pskCommandPending)

        // PSK:[Key]:[Hash]:SET
        assertThat(result).isEqualTo("PSK:${key}:${expectedHash}")
    }
}
