// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import org.gxf.crestdeviceservice.psk.configuration.PskConfiguration
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.exception.InitialKeySetException
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.repository.PskRepository

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

import java.security.SecureRandom
import java.time.Instant

/**
 * @param pskRepository
 * @param pskConfiguration
 */
@Service
class PskService(
    private val pskRepository: PskRepository,
    private val pskConfiguration: PskConfiguration
) {
    private val logger = KotlinLogging.logger {}
    private val secureRandom = SecureRandom.getInstanceStrong()

    fun getCurrentActiveKey(deviceId: String) = getCurrentActivePsk(deviceId)?.preSharedKey

    private fun getCurrentActivePsk(deviceId: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            deviceId, PreSharedKeyStatus.ACTIVE)

    fun getCurrentPendingPsk(deviceId: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            deviceId, PreSharedKeyStatus.PENDING)

    fun getCurrentReadyPsk(deviceId: String): PreSharedKey? {
        val allKeys = pskRepository.findAll()
        logger.debug { allKeys.joinToString { ". " } }
        val result =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                deviceId, PreSharedKeyStatus.READY)
        return result
    }

    fun readyForPskSetCommand(deviceId: String) =
        isReadyPskPresent(deviceId) && !isPendingPskPresent(deviceId)

    fun isPendingPskPresent(deviceId: String): Boolean = getCurrentPendingPsk(deviceId) != null

    private fun isReadyPskPresent(deviceId: String) = getCurrentReadyPsk(deviceId) != null

    fun setPendingKeyAsInvalid(deviceId: String) {
        logger.warn { "Pending key for device $deviceId is set to invalid." }
        val psk =
            (getCurrentPendingPsk(deviceId)
                ?: throw NoExistingPskException("No pending key exists to set as invalid")).apply {
                status = PreSharedKeyStatus.INVALID
            }
        pskRepository.save(psk)
    }

    fun setInitialKeyForDevice(
        deviceId: String,
        psk: String,
        secret: String
    ) {
        logger.info { "Prepare initial key for device $deviceId" }
        if (pskRepository.countByIdentityAndStatus(deviceId, PreSharedKeyStatus.ACTIVE) != 0L) {
            throw InitialKeySetException("Key already exists for device. Key cannot be overridden")
        }
        pskRepository.save(
            PreSharedKey(deviceId, 0, Instant.now(), psk, secret, PreSharedKeyStatus.ACTIVE))
    }

    fun generateNewReadyKeyForDevice(deviceId: String) {
        logger.info { "Creating new ready key for device $deviceId" }
        val newKey = generatePsk()
        val previousPsk =
            getCurrentActivePsk(deviceId)
                ?: throw NoExistingPskException("There is no active key present")
        val newVersion = previousPsk.revision + 1
        pskRepository.save(
            PreSharedKey(
                deviceId,
                newVersion,
                Instant.now(),
                newKey,
                previousPsk.secret,
                PreSharedKeyStatus.READY))
    }

    private fun generatePsk(): String =
        secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray().fold("") { acc, next ->
            acc + ALLOWED_CHARACTERS[next]
        }

    fun setPskToPendingForDevice(deviceId: String): PreSharedKey {
        val psk =
            (getCurrentReadyPsk(deviceId)
                ?: throw NoExistingPskException("There is no new key ready to be set")).apply {
                status = PreSharedKeyStatus.PENDING
            }
        logger.debug { "Save ready psk as pending" }
        return pskRepository.save(psk)
    }

    fun changeInitialPsk() = pskConfiguration.changeInitialPsk

    /**
     * Sets the current, active, key to inactive and the new, pending, key to active and saves both
     * to the pskRepository
     *
     * @param deviceId: identity of device for which to change the keys
     * @throws NoExistingPskException
     */
    @Throws(NoExistingPskException::class)
    fun changeActiveKey(deviceId: String) {
        val currentPsk = getCurrentActivePsk(deviceId)
        val newPsk = getCurrentPendingPsk(deviceId)

        if (currentPsk == null || newPsk == null) {
            throw NoExistingPskException("No current or new psk, impossible to change active key")
        }

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.saveAll(listOf(currentPsk, newPsk))
    }

    companion object {
        private const val ALLOWED_CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val KEY_LENGTH = 16L
    }
}
