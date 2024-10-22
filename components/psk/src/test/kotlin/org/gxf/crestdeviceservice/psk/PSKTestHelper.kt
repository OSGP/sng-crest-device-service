// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import java.time.Instant
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus

object PSKTestHelper {
    fun preSharedKeyReady() = preSharedKeyWithStatus(PreSharedKeyStatus.READY)

    fun preSharedKeyActive() = preSharedKeyWithStatus(PreSharedKeyStatus.ACTIVE)

    fun preSharedKeyPending() = preSharedKeyWithStatus(PreSharedKeyStatus.PENDING)

    private fun preSharedKeyWithStatus(status: PreSharedKeyStatus) =
        PreSharedKey("identity", 1, Instant.now(), "key", status)
}
