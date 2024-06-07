// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.entity.convertors

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {

    private val databaseFieldEncryptor =
        DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldBeAbleTeEncryptAndDecryptData() {
        val expected = "data"
        val encrypted = databaseFieldEncryptor.convertToDatabaseColumn(expected)
        val decrypted = databaseFieldEncryptor.convertToEntityAttribute(encrypted)

        assertThat(encrypted).isNotEqualTo("data")
        assertThat(decrypted).isEqualTo(expected)
    }
}
