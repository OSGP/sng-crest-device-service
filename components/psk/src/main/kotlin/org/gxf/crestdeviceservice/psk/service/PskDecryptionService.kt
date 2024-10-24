// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import java.util.Base64
import javax.crypto.Cipher
import org.gxf.crestdeviceservice.psk.configuration.PskDecryptionConfiguration
import org.gxf.crestdeviceservice.psk.exception.UnknownKeyRefException
import org.springframework.stereotype.Service

@Service
class PskDecryptionService(private val pskDecryptionConfiguration: PskDecryptionConfiguration) {

    fun decryptSecret(encryptedSecret: String, keyRef: String): String {
        val decodedSecret: ByteArray = Base64.getDecoder().decode(encryptedSecret)

        return cypherForKeyRef(keyRef).doFinal(decodedSecret).decodeToString()
    }

    private fun cypherForKeyRef(keyRef: String): Cipher {
        val privateKey =
            pskDecryptionConfiguration.privateKey[keyRef]
                ?: throw UnknownKeyRefException("Keyref not found in configuration")

        return Cipher.getInstance(pskDecryptionConfiguration.method).apply { init(Cipher.DECRYPT_MODE, privateKey) }
    }
}
