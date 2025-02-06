// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import kotlin.math.roundToInt
import org.gxf.crestdeviceservice.model.AlarmsInfo

object AnalogAlarmThresholdCalculator {
    // volt = millibar / 500
    // payload value = volt * 200
    fun getPayloadFromMillibar(millibarValue: Int) = (millibarValue * 0.4).roundToInt()

    fun calculateThresholdsFromDevice(alarmsInfo: AlarmsInfo) =
        alarmsInfo.copy(
            AL6 = alarmsInfo.AL6?.map { getMillibarFromPayload(it) },
            AL7 = alarmsInfo.AL7?.map { getMillibarFromPayload(it) },
        )

    fun getMillibarFromPayload(payloadValue: Int) = (payloadValue * 2.5).roundToInt()
}
