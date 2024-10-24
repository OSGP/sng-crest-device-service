// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.entity

enum class PreSharedKeyStatus {
    /** Generated, but not sent to device */
    READY,
    /** Sent to device, but not acknowledged */
    PENDING,
    /** Acknowledged by device */
    ACTIVE,
    INACTIVE,
    INVALID
}
