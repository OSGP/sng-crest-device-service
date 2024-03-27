package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.psk.PskService
import org.springframework.stereotype.Service

@Service
class URCService(
    private val pskService: PskService
) {
    companion object {
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "ER"
        private const val URC_FIELD = "URC"
    }

    private val logger = KotlinLogging.logger {}

    fun interpretURCInMessage(identity: String, body: JsonNode) {
        val urc = getUrcFromMessage(body)

        if (urc != null) {
            logger.debug { "Received message with urc $urc" }

            when {
                urc.contains(URC_PSK_SUCCESS) -> {
                    check(pskService.isPendingKeyPresent(identity)) { "Success URC received, but no pending key present to set as active" }
                    logger.info { "PSK set successfully, changing active key" }
                    pskService.changeActiveKey(identity)
                }

                urc.contains(URC_PSK_ERROR) -> {
                    check(pskService.isPendingKeyPresent(identity)) { "Failure URC received, but no pending key present to set as invalid" }
                    logger.warn { "Error received for set PSK command, setting pending key to invalid" }
                    pskService.setPendingKeyAsInvalid(identity)
                }
            }
        }
    }

    private fun getUrcFromMessage(body: JsonNode) = body[URC_FIELD]
        .filter { it.isTextual }
        .map { it.asText() }
        .firstOrNull()
}
