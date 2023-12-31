package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.data.entity.PreSharedKey
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

@Service
class PskService(private val pskRepository: PskRepository) {

    companion object {
        private const val KEY_LENGTH = 16L
        private const val ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    fun getCurrentPsk(identity: String) =
            pskRepository.findFirstByIdentityOrderByRevisionTimeDesc(identity)?.preSharedKey

    fun generateAndSetNewKeyForIdentity(identity: String): String {
        val newKey = generatePsk()
        pskRepository.save(PreSharedKey(identity, Instant.now(), newKey))
        return newKey
    }

    fun hasDefaultKey(identity: String): Boolean {
        return pskRepository.countPsksByIdentity(identity) == 1L
    }

    private fun generatePsk(): String {
        val secureRandom = SecureRandom.getInstanceStrong()

        return secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray().fold("") { acc, next -> acc + ALLOWED_CHARACTERS[next] }
    }
}
