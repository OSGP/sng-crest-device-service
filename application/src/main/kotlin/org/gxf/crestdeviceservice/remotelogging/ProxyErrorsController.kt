// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.remotelogging

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/error")
class ProxyErrorsController {

    private val logger = KotlinLogging.logger {}

    @PostMapping
    fun post(@NonNull @RequestBody error: String) {
        logger.error { "Received error from the proxy with message: $error" }
    }
}
