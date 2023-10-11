import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.kafka.KafkaProducer
import org.gxf.crestdeviceservice.service.MessageService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MessageServiceTest {
    @Mock
    private lateinit var mock: KafkaProducer

    @InjectMocks
    private lateinit var messageService: MessageService

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("{}")
        messageService.handleMessage(jsonNode)
        Mockito.verify(mock).produceMessage(jsonNode)
    }
}