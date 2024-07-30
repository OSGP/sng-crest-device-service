package org.gxf.crestdeviceservice.command.repository

import org.gxf.crestdeviceservice.command.entity.Command
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommandRepository : CrudRepository<Command, UUID> {

    fun findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(deviceId: String, type: Command.CommandType): Command?
}
