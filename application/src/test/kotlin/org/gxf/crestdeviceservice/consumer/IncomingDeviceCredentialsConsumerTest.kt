// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import com.alliander.sng.DeviceCredentials
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class IncomingDeviceCredentialsConsumerTest {
    private val deviceService = mock<DeviceService>()
    private val pskService = mock<PskService>()
    private val pskDecryptionService = mock<PskDecryptionService>()
    private val commandService = mock<CommandService>()
    private val incomingDeviceCredentialsConsumer =
        IncomingDeviceCredentialsConsumer(deviceService, pskService, pskDecryptionService, commandService)

    @Test
    fun handleIncomingDeviceCredentialsChangeInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val decryptedPsk = "psk"
        val secret = "encrypted-secret"
        val decryptedSecret = "secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)

        whenever(pskDecryptionService.decryptSecret(psk, keyRef)).thenReturn(decryptedPsk)
        whenever(pskDecryptionService.decryptSecret(secret, keyRef)).thenReturn(decryptedSecret)
        whenever(pskService.changeInitialPsk()).thenReturn(true)

        incomingDeviceCredentialsConsumer.handleIncomingDeviceCredentials(deviceCredentials)

        verify(deviceService).createDevice(imei, decryptedSecret)
        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk)
        verify(pskService).generateNewReadyKeyForDevice(imei)
        verify(commandService).saveCommandEntities(any<List<Command>>())
    }

    @Test
    fun handleIncomingDeviceCredentialsWithoutChangingInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val decryptedPsk = "psk"
        val secret = "encrypted-secret"
        val decryptedSecret = "secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)

        whenever(pskDecryptionService.decryptSecret(psk, keyRef)).thenReturn(decryptedPsk)
        whenever(pskDecryptionService.decryptSecret(secret, keyRef)).thenReturn(decryptedSecret)
        whenever(pskService.changeInitialPsk()).thenReturn(false)

        incomingDeviceCredentialsConsumer.handleIncomingDeviceCredentials(deviceCredentials)

        verify(deviceService).createDevice(imei, decryptedSecret)
        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk)
        verify(pskService, times(0)).generateNewReadyKeyForDevice(imei)
        verify(commandService, times(0)).saveCommandEntities(any<List<Command>>())
    }
}
