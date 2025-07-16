// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import org.gxf.crestdeviceservice.shared.persistence.DatabaseFieldEncryptor
import java.time.Instant

@Entity
@IdClass(PreSharedKeyCompositeKey::class)
class PreSharedKey(
    @Id val identity: String,
    @Id val revision: Int,
    val revisionTime: Instant,
    @Convert(converter = DatabaseFieldEncryptor::class) val preSharedKey: String,
    @Enumerated(EnumType.STRING) var status: PreSharedKeyStatus,
)
