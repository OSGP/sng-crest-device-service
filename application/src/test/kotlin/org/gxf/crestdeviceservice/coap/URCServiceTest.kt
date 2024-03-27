package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.util.ResourceUtils

class URCServiceTest {
    private val pskService = mock<PskService>()
    private val urcService = URCService(pskService)
    private val mapper = spy<ObjectMapper>()

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_success.json")
        val message = mapper.readTree(fileToUse)

        urcService.interpretURCInMessage(identity, message)

        verify(pskService).changeActiveKey(identity)
    }

    @Test
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived() {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_failure.json")
        val message = mapper.readTree(fileToUse)
        urcService.interpretURCInMessage(identity, message)

        verify(pskService).setPendingKeyAsInvalid(identity)
    }
}
