package org.gxf.crestdeviceservice.command.service

import io.github.oshai.kotlinlogging.KotlinLogging
import com.alliander.sng.Command as ExternalCommand
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class CommandService(
    private val commandRepository: CommandRepository
) {

    private val logger = KotlinLogging.logger {}

    private val knownCommands = Command.CommandType.entries.map { it.name }

    /**
     * Check if the incoming command should be rejected.
     *
     * @return An optional string with the reason for rejection, or empty if the command should be accepted.
     */
    fun shouldBeRejected(command: ExternalCommand): Optional<String> {
        if (command.command !in knownCommands) {
            return Optional.of("Unknown command")
        }

        val commandType: Command.CommandType = Command.CommandType.valueOf(command.command)

        if (deviceHasNewerOrPendingCommand(command.deviceId, commandType, command.timestamp)) {
            return Optional.of("Device has a newer or pending command of the same type")
        }

        return Optional.empty()
    }

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a later date.
     * This check prevents issues if commands arrive out of order or if we reset the kafka consumer group.
     */
    fun deviceHasNewerOrPendingCommand(
        deviceId: String,
        commandType: Command.CommandType,
        timestampNewCommand: Instant
    ): Boolean {
        val latestCommandInDatabase =
            commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(deviceId, commandType)

        // No command found for device with the same type
        if (latestCommandInDatabase == null) {
            return false
        }

        // If the device already has a newer command in the database ignore the incoming command
        if(latestCommandInDatabase.timestampIssued.isAfter(timestampNewCommand)){
            return true
        }

        // The latest command is pending or in progress
        if (latestCommandInDatabase.status == CommandStatus.PENDING || latestCommandInDatabase.status == CommandStatus.IN_PROGRESS) {
            return true
        }

        return false
    }

    fun saveCommand(incomingCommand: ExternalCommand) {

        val commandEntity = Command(
            id = UUID.randomUUID(),
            deviceId = incomingCommand.deviceId,
            correlationId = incomingCommand.correlationId,
            timestampIssued = incomingCommand.timestamp,
            type = Command.CommandType.valueOf(incomingCommand.command),
            status = CommandStatus.PENDING,
            commandValue = incomingCommand.value,
        )

        commandRepository.save(commandEntity)
    }
}
