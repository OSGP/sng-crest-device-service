package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.gxf.crestdeviceservice.psk.exception.InitialKeySetException
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

    fun setInitialKeyForIdentify(identity: String, psk: String, secret: String) {
        if (pskRepository.countPsksByIdentity(identity) != 0L) {
            throw InitialKeySetException("Key already exists for identity. Key cannot be overridden")
        }
        pskRepository.save(PreSharedKey(identity, Instant.now(), psk, secret))
    }

    fun generateAndSetNewKeyForIdentity(identity: String): PreSharedKey {
        val newKey = generatePsk()
        val secret = pskRepository.findFirstByIdentityOrderByRevisionTimeDesc(identity)!!.secret
        return pskRepository.save(PreSharedKey(identity, Instant.now(), newKey, secret))
    }

    fun hasDefaultKey(identity: String): Boolean {
        return pskRepository.countPsksByIdentity(identity) == 1L
    }

    private fun generatePsk(): String {
        val secureRandom = SecureRandom.getInstanceStrong()

        return secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray()
                .fold("") { acc, next -> acc + ALLOWED_CHARACTERS[next] }
    }
}
