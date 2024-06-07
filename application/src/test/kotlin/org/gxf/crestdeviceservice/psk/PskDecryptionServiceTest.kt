// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.util.Base64
import javax.crypto.Cipher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.psk.configuration.PskDecryptionConfiguration
import org.gxf.crestdeviceservice.psk.exception.UnknownKeyRefException
import org.junit.jupiter.api.Test

class PskDecryptionServiceTest {

    @Test
    fun decryptSecret() {
        val keyRef = "1"
        val secret = "12345"

        // Create keys and encrypt the test secret
        val keyPair = generateKeyPair()
        val encryptedSecret = createSecret(secret, keyPair.public)

        // Create the testing decryption service
        val decryptionService =
            PskDecryptionService(
                PskDecryptionConfiguration(
                    mapOf(keyRef to keyPair.private as RSAPrivateKey),
                    "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"))

        // Decrypt the secret
        val decryptedSecret = decryptionService.decryptSecret(encryptedSecret, keyRef)

        assertThat(decryptedSecret).isEqualTo(secret)
    }

    @Test
    fun decryptSecretUnknownRef() {
        val keyRef = "1"
        val secret = "12345"

        // Create keys and encrypt the test secret
        val keyPair = generateKeyPair()
        val encryptedSecret = createSecret(secret, keyPair.public)

        // Create the testing decryption service
        val decryptionService =
            PskDecryptionService(
                PskDecryptionConfiguration(
                    mapOf(keyRef to keyPair.private as RSAPrivateKey),
                    "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"))

        assertThatThrownBy { decryptionService.decryptSecret(encryptedSecret, "2") }
            .isInstanceOf(UnknownKeyRefException::class.java)
    }

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(4096) }.generateKeyPair()

    private fun createSecret(secret: String, publicKey: PublicKey) =
        Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
            .apply { init(Cipher.ENCRYPT_MODE, publicKey) }
            .doFinal(secret.toByteArray())
            .let { Base64.getEncoder().encodeToString(it) }
}
