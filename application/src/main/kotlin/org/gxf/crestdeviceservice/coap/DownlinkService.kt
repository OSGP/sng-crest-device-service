// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.springframework.stereotype.Service

@Service
class DownlinkService(private val pskService: PskService) {

    companion object {
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    @Throws(NoExistingPskException::class)
    fun getDownlinkForIdentity(identity: String, body: JsonNode): String {
        logger.debug { "Check if device $identity needs key change" }
        if (pskService.needsKeyChange(identity)) {
            logger.info { "Device $identity needs key change" }
            val newKey = pskService.setReadyKeyForIdentityAsPending(identity)

            // After setting a new psk, the device will send a new message if the psk set was
            // successful
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
            }
            return PskCommandCreator.createPskSetCommand(newKey)
        }

        return RESPONSE_SUCCESS
    }
}
