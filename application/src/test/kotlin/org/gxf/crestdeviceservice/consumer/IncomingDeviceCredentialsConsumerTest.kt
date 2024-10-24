// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import com.alliander.sng.DeviceCredentials
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class IncomingDeviceCredentialsConsumerTest {
    private val pskService = mock<PskService>()
    private val pskDecryptionService = mock<PskDecryptionService>()
    private val commandService = mock<CommandService>()
    private val incomingDeviceCredentialsConsumer =
        IncomingDeviceCredentialsConsumer(pskService, pskDecryptionService, commandService)

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

        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk, decryptedSecret)
        verify(pskService).generateNewReadyKeyForDevice(imei)
        verify(commandService).saveCommands(any<Command>(), any<Command>())
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

        verify(pskService).setInitialKeyForDevice(imei, decryptedPsk, decryptedSecret)
        verify(pskService, never()).generateNewReadyKeyForDevice(imei)
        verify(commandService, never()).saveCommand(any<Command>())
    }
}
