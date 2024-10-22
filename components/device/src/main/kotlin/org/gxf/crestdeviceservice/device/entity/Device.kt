// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.gxf.crestdeviceservice.shared.persistence.DatabaseFieldEncryptor

@Entity class Device(@Id val id: String, @Convert(converter = DatabaseFieldEncryptor::class) val secret: String)
