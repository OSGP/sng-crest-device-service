// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import io.github.oshai.kotlinlogging.KotlinLogging

class Downlink(private val maxSize: Int = 1024) {
    private var downlink = ""

    fun getDownlink() = downlink.ifBlank { RESPONSE_SUCCESS }

    private val logger = KotlinLogging.logger {}

    fun addIfItFits(downlinkToAdd: String): Boolean {
        val currentSize = downlink.length

        val newCumulative =
            if (downlink.isEmpty()) {
                "!$downlinkToAdd"
            } else {
                downlink.plus(";$downlinkToAdd")
            }
        val newSize = newCumulative.length
        logger.debug {
            "Trying to add a downlink '$downlinkToAdd' to existing downlink '$downlink'. " +
                "Current downlink size: $currentSize. Downlink size after after adding: $newSize."
        }
        if (newSize <= maxSize) {
            downlink = newCumulative
            return true
        }
        return false
    }

    companion object {
        const val RESPONSE_SUCCESS = "0"
    }
}
