// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

/**
 * @property message
 */
enum class ErrorUrc(val message: String) {
    CSER("Checksum error"),
    DLER("Downlink (syntax) error"),
    DLNA("Downlink not allowed"),
    EQER("Set PSK does not equal earlier PSK"),
    ERR("Error processing (downlink) value"),
    HSER("SHA256 hash error"),
    UNK("Downlink unknown"),
    ;

    companion object {
        fun getMessageFromCode(code: String): String {
            val error = entries.firstOrNull { code.endsWith(it.name) }
            return error?.message ?: "Unknown URC"
        }
    }
}
