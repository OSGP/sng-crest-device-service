// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.time.Instant
import java.util.UUID

object TestConstants {
    const val DEVICE_ID = "device-id"
    const val MESSAGE_RECEIVED = "Command received"
    val correlationId = UUID.randomUUID()
    val timestamp = Instant.now()
}
