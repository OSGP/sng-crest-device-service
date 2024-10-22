// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.service

import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.exception.DuplicateDeviceException
import org.gxf.crestdeviceservice.device.exception.NoSuchDeviceException
import org.gxf.crestdeviceservice.device.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DeviceServiceTest {
    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val SECRET = "secret"
    }

    @Mock private lateinit var repository: DeviceRepository

    @InjectMocks private lateinit var service: DeviceService

    @Test
    fun `should create device`() {
        whenever(repository.existsById(DEVICE_ID)).thenReturn(false)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val device = service.createDevice(DEVICE_ID, SECRET)
        assertThat(device.id).isEqualTo(DEVICE_ID)
        assertThat(device.secret).isEqualTo(SECRET)
    }

    @Test
    fun `should not create duplicate device`() {
        whenever(repository.existsById(DEVICE_ID)).thenReturn(true)

        assertThatThrownBy { service.createDevice(DEVICE_ID, SECRET) }
            .isInstanceOf(DuplicateDeviceException::class.java)

        verify(repository, never()).save(any())
    }

    @Test
    fun `should retrieve device`() {
        val device = Device(DEVICE_ID, SECRET)

        whenever(repository.findById(DEVICE_ID)).thenReturn(Optional.of(device))

        assertThat(service.getDevice(DEVICE_ID)).isSameAs(device)
    }

    @Test
    fun `should not retrieve nonexistent device`() {
        whenever(repository.findById(DEVICE_ID)).thenReturn(Optional.empty())

        assertThatThrownBy { service.getDevice(DEVICE_ID) }.isInstanceOf(NoSuchDeviceException::class.java)
    }
}
