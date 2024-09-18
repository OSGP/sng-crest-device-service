// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import org.gxf.crestdeviceservice.psk.PskTestHelper
import org.gxf.crestdeviceservice.psk.configuration.PskConfiguration
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey as Psk
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.repository.PskRepository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

typealias PskListCaptor = KArgumentCaptor<List<Psk>>

class PskServiceTest {
    private val pskRepository: PskRepository = mock()
    private val pskConfiguration: PskConfiguration = mock()
    private val pskService = PskService(pskRepository, pskConfiguration)
    private val pskCaptor: KArgumentCaptor<Psk> = argumentCaptor()
    private val pskListCaptor: PskListCaptor = argumentCaptor()

    @Test
    fun getCurrentActiveKey() {
        val psk = PskTestHelper.preSharedKeyActive()
        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                DEVICE_ID, PreSharedKeyStatus.ACTIVE))
            .thenReturn(psk)

        val currentActiveKey = pskService.getCurrentActiveKey(DEVICE_ID)

        assertThat(currentActiveKey).isEqualTo(psk.preSharedKey)
    }

    @Test
    fun pendingKeyPresentTrue() {
        val psk = PskTestHelper.preSharedKeyPending()
        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                DEVICE_ID, PreSharedKeyStatus.PENDING))
            .thenReturn(psk)

        val pendingKeyPresent = pskService.isPendingPskPresent(DEVICE_ID)

        assertThat(pendingKeyPresent).isEqualTo(true)
    }

    @Test
    fun pendingKeyPresentFalse() {
        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                DEVICE_ID, PreSharedKeyStatus.PENDING))
            .thenReturn(null)

        val pendingKeyPresent = pskService.isPendingPskPresent(DEVICE_ID)

        assertThat(pendingKeyPresent).isEqualTo(false)
    }

    @Test
    fun setPendingKeyAsInvalid() {
        val psk = PskTestHelper.preSharedKeyPending()
        val deviceId = psk.identity
        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                deviceId, PreSharedKeyStatus.PENDING))
            .thenReturn(psk)

        pskService.setPendingKeyAsInvalid(deviceId)

        verify(pskRepository).save(pskCaptor.capture())
        assertThat(pskCaptor.firstValue.status).isEqualTo(PreSharedKeyStatus.INVALID)
    }

    @Test
    fun saveReadyKeyForIdentityAsPending() {
        val psk = PskTestHelper.preSharedKeyReady()
        val deviceId = psk.identity
        val status = psk.status

        whenever(pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, status))
            .thenReturn(psk)
        whenever(pskRepository.save(psk)).thenReturn(psk)

        val result = pskService.setPskToPendingForDevice(deviceId)

        verify(pskRepository).save(pskCaptor.capture())
        assertThat(pskCaptor.firstValue.status).isEqualTo(PreSharedKeyStatus.PENDING)
        assertThat(result.status).isEqualTo(PreSharedKeyStatus.PENDING)
    }

    @Test
    fun changeActiveKey() {
        val currentPsk = PskTestHelper.preSharedKeyActive()
        val newPsk = PskTestHelper.preSharedKeyPending()

        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                DEVICE_ID, PreSharedKeyStatus.ACTIVE))
            .thenReturn(currentPsk)
        whenever(
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                DEVICE_ID, PreSharedKeyStatus.PENDING))
            .thenReturn(newPsk)

        pskService.changeActiveKey(DEVICE_ID)

        verify(pskRepository).saveAll(pskListCaptor.capture())
        val actualSavedCurrentPsk = pskListCaptor.firstValue[0]
        val actualSavedNewPsk = pskListCaptor.firstValue[1]
        assertThat(actualSavedCurrentPsk.status).isEqualTo(PreSharedKeyStatus.INACTIVE)
        assertThat(actualSavedNewPsk.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
    }

    companion object {
        const val DEVICE_ID = "identity"
    }
}
