// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.util.ResourceUtils

object MessageFactory {
    private const val URC_FIELD = "URC"
    private const val DL_FIELD = "DL"

    private val mapper = ObjectMapper()

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }

    fun messageWithUrc(urcs: List<String>, downlink: String = ""): JsonNode {
        val urcNodes = urcs.map { urc -> TextNode(urc) }
        val downlinkNode = ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(downlink)))

        val urcFieldValue: ArrayNode = ObjectMapper().valueToTree(urcNodes + listOf(downlinkNode))

        val message = messageTemplate()
        message.replace(URC_FIELD, urcFieldValue)

        return message
    }
}
