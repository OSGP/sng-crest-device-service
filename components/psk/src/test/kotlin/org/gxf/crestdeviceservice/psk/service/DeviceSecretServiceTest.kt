// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import java.time.Instant
import org.assertj.core.api.Assertions.*
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DeviceSecretServiceTest {
    @Mock private lateinit var pskService: PskService
    @InjectMocks private lateinit var deviceSecretService: DeviceSecretService

    @Test
    fun shouldGetSecret() {
        val deviceId = "test-device"
        val expectedSecret = "this is the secret"
        val psk = PreSharedKey(deviceId, 0, Instant.now(), "n/a", expectedSecret, PreSharedKeyStatus.ACTIVE)
        whenever(pskService.getCurrentActivePsk(deviceId)).thenReturn(psk)

        val actualSecret = deviceSecretService.getDeviceSecret(deviceId)
        assertThat(actualSecret).isEqualTo(expectedSecret)
    }
}
