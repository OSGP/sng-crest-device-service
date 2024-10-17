// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import java.io.Serializable

data class FirmwarePacketCompositeKey(var firmware: Firmware?, var packetNumber: Int?) : Serializable {
    constructor() : this(null, null)
}
