// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.util.ResourceUtils
import java.time.Instant

class DownlinkServiceTest {
    private val pskService = mock<PskService>()
    private val urcService = mock<URCService>()
    private val downLinkService = DownlinkService(pskService, urcService)
    private val mapper = spy<ObjectMapper>()

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val identity = "identity"
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"
        val psk = PreSharedKey(
            identity,
            1,
            Instant.now(),
            expectedKey,
            "secret",
            PreSharedKeyStatus.PENDING
        )

        whenever(pskService.needsKeyChange(identity)).thenReturn(true)
        whenever(pskService.setReadyKeyForIdentityAsPending(identity)).thenReturn(psk)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message.json")
        val message = mapper.readTree(fileToUse)

        val result = downLinkService.getDownlinkForIdentity(identity, message)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]SET
        assertThat(result).isEqualTo("!PSK:$expectedKey:$expectedHash;PSK:$expectedKey:${expectedHash}SET")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        val identity = "identity"
        whenever(pskService.needsKeyChange(identity)).thenReturn(false)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message.json")
        val message = mapper.readTree(fileToUse)

        val result = downLinkService.getDownlinkForIdentity(identity, message)

        assertThat(result).isEqualTo("0")
    }
}
