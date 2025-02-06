// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service

@Service
class DeviceMessageService(
    private val messageProducerService: MessageProducerService,
    private val downlinkService: DownlinkService,
    private val payloadService: PayloadService,
) {
    private val locks: MutableMap<String, Any> = mutableMapOf()

    fun processDeviceMessage(message: JsonNode, deviceId: String): String {
        messageProducerService.produceMessage(message)

        synchronized(lock(deviceId)) {
            val downlink = downlinkService.createDownlink()
            payloadService.processPayload(deviceId, message, downlink)
            downlinkService.getDownlinkForDevice(deviceId, downlink)
            return downlink.getDownlink()
        }
    }

    @Synchronized
    private fun lock(substationIdentification: String) = locks.computeIfAbsent(substationIdentification) { _ -> Any() }
}
