package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class CommandServiceTest {
    private val commandRepository = mock<CommandRepository>()
    private val commandService = CommandService(commandRepository)

    @Test
    fun `Check if command is allowed`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity())

        val result = commandService.shouldBeRejected(testExternalCommand())
        assertThat(result).isEmpty
    }

    @Test
    fun `Check if command is not allowed when command is unknown`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(status = Command.CommandStatus.PENDING))

        val command =  testExternalCommand()
        command.command = "UNKNOWN"
        val result = commandService.shouldBeRejected(command)
        assertThat(result).isNotEmpty
    }


    @Test
    fun `Check if command is not allowed when latest command is pending`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(status = Command.CommandStatus.PENDING))

        val result = commandService.shouldBeRejected(testExternalCommand())
        assertThat(result).isNotEmpty
    }

    @Test
    fun `Check if command is not allowed when latest is in the future`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(timestampIssued = Instant.now().plusSeconds(100)))

        val result = commandService.shouldBeRejected(testExternalCommand())
        assertThat(result).isNotEmpty
    }


    private val deviceId = "device-id"

    private fun testExternalCommand() = ExternalCommand.newBuilder()
        .setDeviceId(deviceId)
        .setCorrelationId(UUID.randomUUID())
        .setTimestamp(Instant.now())
        .setCommand(Command.CommandType.REBOOT.name)
        .setValue(null)
        .build()

    private fun testCommandEntity() = Command(
        id = UUID.randomUUID(),
        deviceId = deviceId,
        correlationId = UUID.randomUUID(),
        timestampIssued = Instant.now(),
        type = Command.CommandType.REBOOT,
        commandValue = null,
        status = Command.CommandStatus.SUCCESSFUL
    )
}
