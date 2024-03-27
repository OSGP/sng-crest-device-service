// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.psk.entity.convertors

import jakarta.persistence.AttributeConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


@Component
class DatabaseFieldEncryptor : AttributeConverter<String, String> {

    companion object {
        private const val ENCRYPTION_METHOD = "AES/GCM/NoPadding"
        private const val ENCRYPTION_ALGORITHM = "AES"
        private const val IV_SIZE = 16
    }

    @Value("\${crest-device-service.database.encryption-key}")
    lateinit var secret: String

    override fun convertToDatabaseColumn(attribute: String): String {
        return Base64.getEncoder().encodeToString(encrypt(attribute, SecretKeySpec(secret.toByteArray(), ENCRYPTION_ALGORITHM)))
    }

    override fun convertToEntityAttribute(dbData: String): String {
        return decrypt(Base64.getDecoder().decode(dbData), SecretKeySpec(secret.toByteArray(), ENCRYPTION_ALGORITHM))
    }

    private fun encrypt(plaintext: String, secretKey: SecretKey): ByteArray {
        val iv = ByteArray(IV_SIZE)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance(ENCRYPTION_METHOD)
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        val cipherText = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(iv.size + cipherText.size)
        byteBuffer.put(iv)
        byteBuffer.put(cipherText)
        return byteBuffer.array()
    }

    private fun decrypt(cipherMessage: ByteArray, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(ENCRYPTION_METHOD)
        val gcmIv: AlgorithmParameterSpec = GCMParameterSpec(128, cipherMessage, 0, IV_SIZE)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv)
        val plainText = cipher.doFinal(cipherMessage, IV_SIZE, cipherMessage.size - IV_SIZE)
        return String(plainText, StandardCharsets.UTF_8)
    }
}
