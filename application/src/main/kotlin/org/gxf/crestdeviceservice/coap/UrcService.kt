// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.springframework.stereotype.Service

@Service
class UrcService(private val pskService: PskService) {
    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
    }

    private val logger = KotlinLogging.logger {}

    @Throws(NoExistingPskException::class)
    fun interpretURCInMessage(identity: String, body: JsonNode) {
        val urcs = getUrcsFromMessage(body)
        if (urcs.isEmpty()) {
            logger.debug { "Received message without urcs" }
            return
        }
        logger.debug { "Received message with urcs ${urcs.joinToString(", ")}" }

        when {
            urcsContainPskError(urcs) -> {
                handlePskErrors(identity, urcs)
            }
            urcsContainPskSuccess(urcs) -> {
                handlePskSuccess(identity)
            }
        }
    }

    private fun getUrcsFromMessage(body: JsonNode) =
        body[URC_FIELD].filter { it.isTextual }.map { it.asText() }

    private fun urcsContainPskError(urcs: List<String>) =
        urcs.any { urc -> PskErrorUrc.isPskErrorURC(urc) }

    private fun handlePskErrors(identity: String, urcs: List<String>) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Failure URC received, but no pending key present to set as invalid")
        }

        urcs
            .filter { urc -> PskErrorUrc.isPskErrorURC(urc) }
            .forEach { urc ->
                logger.warn {
                    "PSK set failed for device with id ${identity}: ${PskErrorUrc.messageFromCode(urc)}"
                }
            }

        pskService.setPendingKeyAsInvalid(identity)
    }

    private fun urcsContainPskSuccess(urcs: List<String>) =
        urcs.any { urc -> urc.contains(URC_PSK_SUCCESS) }

    private fun handlePskSuccess(identity: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Success URC received, but no pending key present to set as active")
        }
        logger.info { "PSK set successfully, changing active key" }
        pskService.changeActiveKey(identity)
    }
}
