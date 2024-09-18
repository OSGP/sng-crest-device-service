// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

/**
 * @param meterRegistry
 */
@Service
class MetricService(meterRegistry: MeterRegistry) {
    private val identityInvalidCounter =
        Counter.builder("$METRIC_PREFIX.psk.notfound")
            .description(
                "Counts the number of times a device tried to connect with a wrong/unknown Identity")
            .register(meterRegistry)

    fun incrementIdentityInvalidCounter() = identityInvalidCounter.increment()

    companion object {
        const val METRIC_PREFIX = "deviceservice"
    }
}
