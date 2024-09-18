// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService

import com.alliander.sng.DeviceCredentials
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class IncomingDeviceCredentialsConsumerTest {
    val pskService: PskService = mock()
    val pskDecryptionService: PskDecryptionService = mock()
    val commandService: CommandService = mock()
    val incomingDeviceCredentialsConsumer = IncomingDeviceCredentialsConsumer(pskService, pskDecryptionService,
        commandService)

    @Test
    fun handleIncomingDeviceCredentialsChangeInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val secret = "encrypted-secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)
        
        val decryptedPsk = "psk"
        whenever(pskDecryptionService.decryptSecret(psk, keyRef)).thenReturn(decryptedPsk)

        val decryptedSecret = "secret"
        whenever(pskDecryptionService.decryptSecret(secret, keyRef)).thenReturn(decryptedSecret)

        whenever(pskService.changeInitialPsk()).thenReturn(true)
        
        incomingDeviceCredentialsConsumer.handleIncomingDeviceCredentials(deviceCredentials)

        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk, decryptedSecret)
        verify(pskService).generateNewReadyKeyForDevice(imei)
        verify(commandService).saveCommandEntities(any<List<Command>>())
    }

    @Test
    fun handleIncomingDeviceCredentialsWithoutChangingInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val secret = "encrypted-secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)
        
        val decryptedPsk = "psk"
        whenever(pskDecryptionService.decryptSecret(psk, keyRef)).thenReturn(decryptedPsk)
        val decryptedSecret = "secret"
        whenever(pskDecryptionService.decryptSecret(secret, keyRef)).thenReturn(decryptedSecret)

        whenever(pskService.changeInitialPsk()).thenReturn(false)
        
        incomingDeviceCredentialsConsumer.handleIncomingDeviceCredentials(deviceCredentials)

        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk, decryptedSecret)
        verify(pskService, times(0)).generateNewReadyKeyForDevice(imei)
        verify(commandService, times(0)).saveCommandEntities(any<List<Command>>())
    }
}
