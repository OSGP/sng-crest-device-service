// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlarmsInfo(
    @get:JsonProperty("tamper") val AL0: List<Int>? = null,
    @get:JsonProperty("digital") val AL1: List<Int>? = null,
    @get:JsonProperty("T1") val AL2: List<Int>? = null,
    @get:JsonProperty("H1") val AL3: List<Int>? = null,
    @get:JsonProperty("1") val AL4: List<Int>? = null,
    @get:JsonProperty("2") val AL5: List<Int>? = null,
    @get:JsonProperty("3") val AL6: List<Int>? = null,
    @get:JsonProperty("4") val AL7: List<Int>? = null,
)
