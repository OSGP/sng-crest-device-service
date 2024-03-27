// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.kafka.MessageProducer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MessageServiceTest {
    @Mock
    private lateinit var mock: MessageProducer

    @InjectMocks
    private lateinit var messageService: MessageService

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("{}")
        messageService.handleMessage(jsonNode)
        verify(mock).produceMessage(jsonNode)
    }
}
