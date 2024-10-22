// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.shared.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {
    private val databaseFieldEncryptor = DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldBeAbleTeEncryptAndDecryptData() {
        val expected = "datadatadatadata"

        val encrypted = databaseFieldEncryptor.convertToDatabaseColumn(expected)
        val decrypted = databaseFieldEncryptor.convertToEntityAttribute(encrypted)

        assertThat(encrypted).isNotEqualTo(expected)
        assertThat(decrypted).isEqualTo(expected)
    }
}
