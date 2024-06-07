// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.mockito.kotlin.spy
import org.springframework.util.ResourceUtils

object TestHelper {
    private val mapper = spy<ObjectMapper>()

    fun preSharedKeyReady() = preSharedKeyWithStatus(PreSharedKeyStatus.READY)

    fun preSharedKeyActive() = preSharedKeyWithStatus(PreSharedKeyStatus.ACTIVE)

    fun preSharedKeyPending() = preSharedKeyWithStatus(PreSharedKeyStatus.PENDING)

    private fun preSharedKeyWithStatus(status: PreSharedKeyStatus) =
        PreSharedKey("identity", 1, Instant.now(), "key", "secret", status)

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }
}
