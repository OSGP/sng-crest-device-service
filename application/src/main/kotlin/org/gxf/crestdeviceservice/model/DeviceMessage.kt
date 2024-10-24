// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import com.fasterxml.jackson.databind.JsonNode

class DeviceMessage(val message: JsonNode) {
    fun getFotaMessageCounter() = message[FMC_FIELD].intValue()

    companion object {
        const val FMC_FIELD = "FMC"
    }
}
