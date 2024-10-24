// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.gxf.crestdeviceservice.service.MetricService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/psk")
class PskController(private val pskService: PskService, private val metricService: MetricService) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun getPsk(@RequestHeader("x-device-identity") identity: String): ResponseEntity<String> {
        try {
            val currentPsk = pskService.getCurrentActiveKey(identity)
            return ResponseEntity.ok(currentPsk)
        } catch (_: NoExistingPskException) {
            logger.error { "No psk found for device $identity" }
            metricService.incrementIdentityInvalidCounter()
            return ResponseEntity.notFound().build()
        }
    }
}
