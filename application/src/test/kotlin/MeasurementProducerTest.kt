import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.kafka.MeasurementProducer
import org.gxf.crestdeviceservice.kafka.configuration.KafkaProducerProperties
import org.gxf.sng.avro.DeviceMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class MeasurementProducerTest {

    @Mock
    private lateinit var mockedKafkaTemplate: KafkaTemplate<String, DeviceMessage>

    @Mock
    private lateinit var mockedKafkaProducerProperties: KafkaProducerProperties

    @InjectMocks
    private lateinit var measurementProducer: MeasurementProducer

    @BeforeEach
    fun setup() {
        `when`(mockedKafkaProducerProperties.topicName).thenReturn("topic")
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("""
            {
                "ID":12345
            }
        """)
        measurementProducer.produceMessage(jsonNode)
        verify(mockedKafkaTemplate).send(
                check { assertThat(it).isEqualTo("topic") }, check { assertThat(it.payload).isEqualTo(jsonNode.toString()) })
    }
}
