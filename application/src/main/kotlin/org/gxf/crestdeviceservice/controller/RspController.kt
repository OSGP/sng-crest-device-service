// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RspController(private val commandService: CommandService) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("test/{deviceId}/rsp")
    fun addRspCommand(@PathVariable deviceId: String): ResponseEntity<Unit> {
        saveRspCommand(deviceId, Command.CommandType.RSP)

        return ResponseEntity.ok().build()
    }

    @PostMapping("test/{deviceId}/rsp2")
    fun addRsp2Command(@PathVariable deviceId: String): ResponseEntity<Unit> {
        saveRspCommand(deviceId, Command.CommandType.RSP2)

        return ResponseEntity.ok().build()
    }

    private fun saveRspCommand(deviceId: String, commandType: Command.CommandType) {
        logger.info { "received $commandType command for device '$deviceId'" }

        commandService.save(
            Command(
                id = UUID.randomUUID(),
                deviceId = deviceId,
                correlationId = UUID.randomUUID(),
                timestampIssued = Instant.now(),
                type = commandType,
                status = Command.CommandStatus.PENDING,
                commandValue = null))
    }
}
