// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.dto

data class FirmwareDTO(val name: String, val packets: List<String>) {
    constructor() : this("", listOf())
}
