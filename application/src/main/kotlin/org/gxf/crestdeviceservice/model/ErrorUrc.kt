// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

enum class ErrorUrc(val code: String, val message: String) {
    UNK("UNK", "Downlink unknown"),
    EQER("EQER", "Set PSK does not equal earlier PSK"),
    DLNA("DLNA", "Downlink not allowed"),
    DLER("DLER", "Downlink (syntax) error"),
    HSER("HSER", "SHA256 hash error"),
    CSER("CSER", "Checksum error"),
    ERR("ERR", "Error processing (downlink) value");

    companion object {
        fun messageFromCode(code: String): String {
            val error = entries.firstOrNull { code.endsWith(it.code) }
            return error?.message ?: "Unknown URC"
        }
    }
}