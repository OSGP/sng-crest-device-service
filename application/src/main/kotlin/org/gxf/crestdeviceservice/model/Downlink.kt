// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * @property maxSize
 */
data class Downlink(val maxSize: Int) {
    var downlink = ""
        private set

    private val logger = KotlinLogging.logger {}

    // TODO fix this warning
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
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
}
