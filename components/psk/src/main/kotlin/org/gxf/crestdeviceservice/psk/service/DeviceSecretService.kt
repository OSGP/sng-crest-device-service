// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import org.springframework.stereotype.Component

@Component
class DeviceSecretService(private val pskService: PskService) {
    fun getDeviceSecret(deviceId: String) = pskService.getCurrentActivePsk(deviceId).secret
}
