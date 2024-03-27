// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import org.assertj.core.api.Assertions
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Instant

class PskCommandCreatorTest {

    @ParameterizedTest
    @CsvSource(
            "1234567890123456,ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071,secret",
            "1234567890123456,78383f73855e7595f8d31ee7cabdf854bc4e70d036f225f8d144d566083c7d01,different-secret",
            "6543210987654321,5e15cf0f8a55b58a54f51dda17c1d1645ebc145f912888ec2e02a55d7b7baea4,secret",
            "6543210987654321,64904d94590a354cecd8e65630289bcc22103c07b08c009b0b12a8ef0d58af9d,different-secret"
    )
    fun shouldCreateACorrectPskCommandoWithHash(key: String, expectedHash: String, usedSecret: String) {
        val preSharedKey =
            PreSharedKey("identity", 0, Instant.now(), key, usedSecret, PreSharedKeyStatus.PENDING)

        val result = PskCommandCreator.createPskSetCommand(preSharedKey)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]SET
        Assertions.assertThat(result).isEqualTo("!PSK:$key:$expectedHash;PSK:$key:${expectedHash}SET")
    }
}
