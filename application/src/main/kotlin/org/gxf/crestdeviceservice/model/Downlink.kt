// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import io.github.oshai.kotlinlogging.KotlinLogging

class Downlink(private val maxSize: Int = 1024) {
    private var downlink = ""

    /**
     * Returns the downlink. This is either "0" when no commands were added or a '!', followed by a ';' separated string
     * of the added commands
     *
     * @return the downlink
     */
    fun getDownlink() = downlink.ifEmpty { RESPONSE_SUCCESS }

    private val logger = KotlinLogging.logger {}

    /**
     * Try to add the command to the downlink. Reasons for not adding the command:
     * - new size exceeds `maxSize`
     * - downlink contains an OTA command (no other commands allowed)
     * - OTA command can't be added to other commands in the same downlink
     *
     * @param commandToAdd The command you want to add to the downlink
     * @return true if the command was added, false otherwise
     */
    fun addIfPossible(commandToAdd: String): Boolean =
        when {
            isFirmwareUpdate(commandToAdd) && downlink.isNotEmpty() -> {
                logger.debug { "Not adding OTA, because other commands are already present" }
                false
            }
            isFirmwareUpdate(downlink) -> {
                logger.debug { "Not adding $downlink because the current downlink contains an OTA" }
                false
            }
            else -> {
                addIfItFits(commandToAdd, downlink.length)
            }
        }

    private fun addIfItFits(commandToAdd: String, currentSize: Int): Boolean {
        val newCumulative =
            if (downlink.isEmpty()) {
                FORCE_RESPONSE + commandToAdd
            } else {
                downlink + COMMAND_SEPARATOR + commandToAdd
            }
        val newSize = newCumulative.length
        logger.debug {
            "Trying to add a downlink '$commandToAdd' to existing downlink '$downlink'. " +
                "Current downlink size: $currentSize. Downlink size after after adding: $newSize."
        }
        if (newSize <= maxSize) {
            downlink = newCumulative
            return true
        }
        return false
    }

    private fun isFirmwareUpdate(command: String) = command.matches(otaRegex)

    companion object {
        const val RESPONSE_SUCCESS = "0"
        private const val FORCE_RESPONSE = "!"
        private const val COMMAND_SEPARATOR = ";"
        private val otaRegex = ".*OTA[0-9A-F]{4}:.*".toRegex()
    }
}
