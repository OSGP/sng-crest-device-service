// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import javax.crypto.Cipher
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import org.junit.jupiter.api.Test

class PSKKeyGenerator {

    private val logger = KotlinLogging.logger {}
    private val algorithm = "RSA"

    @Test
    fun generateKeyPair() {
        val generator = KeyPairGenerator.getInstance(algorithm).apply { initialize(4096) }

        val keyPair: KeyPair = generator.generateKeyPair()
        val privateKeyString = privateKeyToString(keyPair.private)
        val publicKeyString = publicKeyToString(keyPair.public)

        val cipher =
            Cipher.getInstance(algorithm).apply { init(Cipher.ENCRYPT_MODE, keyPair.public) }
        logger.info {
            "PSK: " +
                Base64.getEncoder().encodeToString(cipher.doFinal("ABCDEFGHIJKLMNOP".toByteArray()))
        }
        logger.info {
            "Secret: " + Base64.getEncoder().encodeToString(cipher.doFinal("123456".toByteArray()))
        }

        logger.info { "Private Key:\n${privateKeyString}" }
        logger.info { "Public Key:\n${publicKeyString}" }

        val tempDirectory = createTempDirectory()

        File(tempDirectory.absolutePathString(), "private.pkcs8")
            .also { logger.info { "Writing private key to ${it.path}" } }
            .also { it.createNewFile() }
            .writeText(privateKeyString)

        File(tempDirectory.absolutePathString(), "public.x509")
            .also { logger.info { "Writing public key to ${it.path}" } }
            .also { it.createNewFile() }
            .writeText(publicKeyString)
    }

    private fun privateKeyToString(privateKey: PrivateKey) =
        Base64.getEncoder()
            .encodeToString(privateKey.encoded)
            .chunked(Int.MAX_VALUE)
            .joinToString(
                separator = "\n",
                prefix = "-----BEGIN PRIVATE KEY-----\n",
                postfix = "\n-----END PRIVATE KEY-----")

    private fun publicKeyToString(publicKey: PublicKey) =
        Base64.getEncoder()
            .encodeToString(publicKey.encoded)
            .chunked(32)
            .joinToString(
                separator = "\n",
                prefix = "-----BEGIN PUBLIC KEY-----\n",
                postfix = "\n-----END PUBLIC KEY-----")
}
