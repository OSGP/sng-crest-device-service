// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.SecureRandom
import java.time.Instant
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.gxf.crestdeviceservice.psk.exception.InitialKeySetException
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.springframework.stereotype.Service

@Service
class PskService(
    private val pskRepository: PskRepository,
    private val pskConfiguration: PskConfiguration
) {

    companion object {
        private const val KEY_LENGTH = 16L
        private const val ALLOWED_CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    private val logger = KotlinLogging.logger {}

    private val secureRandom = SecureRandom.getInstanceStrong()

    fun getCurrentActiveKey(identity: String) = getCurrentActivePsk(identity)?.preSharedKey

    private fun getCurrentActivePsk(identity: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            identity, PreSharedKeyStatus.ACTIVE)

    fun isPendingKeyPresent(identity: String) = getCurrentPendingKey(identity) != null

    @Throws(NoExistingPskException::class)
    fun setPendingKeyAsInvalid(identity: String) {
        val psk =
            getCurrentPendingKey(identity)
                ?: throw NoExistingPskException("No pending key exists to set as invalid")
        psk.status = PreSharedKeyStatus.INVALID
        pskRepository.save(psk)
    }

    private fun getCurrentPendingKey(identity: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            identity, PreSharedKeyStatus.PENDING)

    @Throws(InitialKeySetException::class)
    fun setInitialKeyForIdentity(identity: String, psk: String, secret: String) {
        if (pskRepository.countByIdentityAndStatus(identity, PreSharedKeyStatus.ACTIVE) != 0L) {
            throw InitialKeySetException(
                "Key already exists for identity. Key cannot be overridden")
        }
        pskRepository.save(
            PreSharedKey(identity, 0, Instant.now(), psk, secret, PreSharedKeyStatus.ACTIVE))
    }

    @Throws(NoExistingPskException::class)
    fun generateNewReadyKeyForIdentity(identity: String) {
        val newKey = generatePsk()
        val previousPSK =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity, PreSharedKeyStatus.ACTIVE)
                ?: throw NoExistingPskException("There is no active key present")
        val newVersion = previousPSK.revision + 1
        pskRepository.save(
            PreSharedKey(
                identity,
                newVersion,
                Instant.now(),
                newKey,
                previousPSK.secret,
                PreSharedKeyStatus.READY))
    }

    private fun generatePsk(): String =
        secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray().fold("") { acc, next
            ->
            acc + ALLOWED_CHARACTERS[next]
        }

    fun setReadyKeyForIdentityAsPending(identity: String): PreSharedKey {
        val readyPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity, PreSharedKeyStatus.READY)
                ?: throw NoExistingPskException("There is no new key ready to be set")
        readyPsk.status = PreSharedKeyStatus.PENDING
        logger.debug { "Save ready psk as pending" }
        return pskRepository.save(readyPsk)
    }

    fun needsKeyChange(identity: String) =
        changeInitialPsk() && readyKeyExists(identity) && !pendingKeyExists(identity)

    private fun readyKeyExists(identity: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            identity, PreSharedKeyStatus.READY) != null

    private fun pendingKeyExists(identity: String) =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            identity, PreSharedKeyStatus.PENDING) != null

    fun changeInitialPsk() = pskConfiguration.changeInitialPsk

    /**
     * Sets the current, active, key to inactive and the new, pending, key to active and saves both
     * to the pskRepository
     *
     * @param identity: identity of device for which to change the keys
     */
    @Throws(NoExistingPskException::class)
    fun changeActiveKey(identity: String) {
        val currentPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity, PreSharedKeyStatus.ACTIVE)
        val newPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity, PreSharedKeyStatus.PENDING)

        if (currentPsk == null || newPsk == null) {
            throw NoExistingPskException("No current or new psk, impossible to change active key")
        }

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.saveAll(listOf(currentPsk, newPsk))
    }
}
