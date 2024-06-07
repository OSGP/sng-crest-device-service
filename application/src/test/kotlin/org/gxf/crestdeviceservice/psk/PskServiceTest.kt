// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PskServiceTest {

    @Mock private lateinit var pskRepository: PskRepository

    @Mock private lateinit var pskConfiguration: PskConfiguration

    @InjectMocks private lateinit var pskService: PskService

    @Captor private lateinit var pskCaptor: ArgumentCaptor<PreSharedKey>

    @Captor private lateinit var pskListCaptor: ArgumentCaptor<List<PreSharedKey>>

    @Test
    fun getCurrentActiveKey() {
        val identity = "identity"
        val psk = TestHelper.preSharedKeyActive()
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.ACTIVE))
            .thenReturn(psk)

        val currentActiveKey = pskService.getCurrentActiveKey(identity)

        assertThat(currentActiveKey).isEqualTo(psk.preSharedKey)
    }

    @Test
    fun pendingKeyPresentTrue() {
        val identity = "identity"
        val psk = TestHelper.preSharedKeyPending()
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.PENDING))
            .thenReturn(psk)

        val pendingKeyPresent = pskService.isPendingKeyPresent(identity)

        assertThat(pendingKeyPresent).isEqualTo(true)
    }

    @Test
    fun pendingKeyPresentFalse() {
        val identity = "identity"
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.PENDING))
            .thenReturn(null)

        val pendingKeyPresent = pskService.isPendingKeyPresent(identity)

        assertThat(pendingKeyPresent).isEqualTo(false)
    }

    @Test
    fun setPendingKeyAsInvalid() {
        val psk = TestHelper.preSharedKeyPending()
        val identity = psk.identity
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.PENDING))
            .thenReturn(psk)

        pskService.setPendingKeyAsInvalid(identity)

        verify(pskRepository).save(pskCaptor.capture())
        assertThat(pskCaptor.value.status).isEqualTo(PreSharedKeyStatus.INVALID)
    }

    @Test
    fun saveReadyKeyForIdentityAsPending() {
        val psk = TestHelper.preSharedKeyReady()
        val identity = psk.identity
        val status = psk.status

        whenever(pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(identity, status))
            .thenReturn(psk)
        whenever(pskRepository.save(psk)).thenReturn(psk)

        val result = pskService.setReadyKeyForIdentityAsPending(identity)

        verify(pskRepository).save(pskCaptor.capture())
        assertThat(pskCaptor.value.status).isEqualTo(PreSharedKeyStatus.PENDING)
        assertThat(result.status).isEqualTo(PreSharedKeyStatus.PENDING)
    }

    @Test
    fun needsKeyChangeReturnsTrueWhenChangeInitialPskIsTrueAndReadyKeyIsPresent() {
        val psk = TestHelper.preSharedKeyReady()
        val identity = psk.identity
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.READY))
            .thenReturn(psk)
        whenever(pskConfiguration.changeInitialPsk).thenReturn(true)

        val needsKeyChange = pskService.needsKeyChange(identity)

        assertThat(needsKeyChange).isTrue()
    }

    @Test
    fun needsKeyChangeReturnsFalseWhenChangeInitialPskIsFalse() {
        whenever(pskConfiguration.changeInitialPsk).thenReturn(false)

        val needsKeyChange = pskService.needsKeyChange("123")

        assertThat(needsKeyChange).isFalse()
    }

    @Test
    fun needsKeyChangeReturnFalseWhenChangeInitialPskIsTrueButReadyKeyIsNotPresent() {
        val identity = "123"
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.READY))
            .thenReturn(null)
        whenever(pskConfiguration.changeInitialPsk).thenReturn(true)

        val needsKeyChange = pskService.needsKeyChange(identity)

        assertThat(needsKeyChange).isFalse()
    }

    @Test
    fun changeActiveKey() {
        val identity = "identity"
        val currentPsk = TestHelper.preSharedKeyActive()
        val newPsk = TestHelper.preSharedKeyPending()

        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.ACTIVE))
            .thenReturn(currentPsk)
        whenever(
                pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                    identity, PreSharedKeyStatus.PENDING))
            .thenReturn(newPsk)

        pskService.changeActiveKey(identity)

        verify(pskRepository).saveAll(pskListCaptor.capture())
        val actualSavedCurrentPsk = pskListCaptor.value[0]
        val actualSavedNewPsk = pskListCaptor.value[1]
        assertThat(actualSavedCurrentPsk.status).isEqualTo(PreSharedKeyStatus.INACTIVE)
        assertThat(actualSavedNewPsk.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
    }
}
