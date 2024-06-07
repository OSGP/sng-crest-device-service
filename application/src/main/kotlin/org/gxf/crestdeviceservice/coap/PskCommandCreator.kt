// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey

object PskCommandCreator {

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "!PSK:${newKey}:${hash};PSK:${newKey}:${hash}:SET"
    }
}
