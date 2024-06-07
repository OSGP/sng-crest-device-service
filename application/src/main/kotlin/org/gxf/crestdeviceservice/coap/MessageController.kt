// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sng")
class MessageController(
    private val messageService: MessageService,
    private val downlinkService: DownlinkService,
    private val urcService: UrcService
) {

    private val logger = KotlinLogging.logger {}

    private val locks: MutableMap<String, Any> = mutableMapOf()

    /**
     * This endpoint handles incoming crest device messages. Responses are generated synchronously
     * to avoid sending the same downlink twice.
     */
    @PostMapping("/{identity}")
    fun post(
        @NonNull @PathVariable identity: String,
        @NonNull @RequestBody body: JsonNode
    ): ResponseEntity<String> {

        logger.debug { "Processing message $body" }
        messageService.handleMessage(body)
        logger.debug { "Processed message" }

        synchronized(lock(identity)) {
            try {
                urcService.interpretURCInMessage(identity, body)
                val downlink = downlinkService.getDownlinkForIdentity(identity, body)
                return ResponseEntity.ok(downlink)
            } catch (e: Exception) {
                logger.error(e) {
                    "Exception occurred while creating downlink for device $identity"
                }
                return ResponseEntity.internalServerError().build()
            }
        }
    }

    @Synchronized
    private fun lock(substationIdentification: String) =
        locks.computeIfAbsent(substationIdentification) { _ -> Any() }
}
