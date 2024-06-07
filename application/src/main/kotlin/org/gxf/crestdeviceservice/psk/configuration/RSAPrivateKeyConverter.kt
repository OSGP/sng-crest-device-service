// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.configuration

import java.io.ByteArrayInputStream
import java.security.interfaces.RSAPrivateKey
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.security.converter.RsaKeyConverters
import org.springframework.stereotype.Component

/** Decodes a pkcs8 RSA Private key string to an object */
@Component
@ConfigurationPropertiesBinding
class RSAPrivateKeyConverter : Converter<String, RSAPrivateKey> {
    override fun convert(from: String): RSAPrivateKey =
        RsaKeyConverters.pkcs8().convert(ByteArrayInputStream(from.toByteArray()))!!
}
