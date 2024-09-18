// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.TestConstants.correlationId as CORRELATION_ID
import org.gxf.crestdeviceservice.TestConstants.timestamp
import org.gxf.crestdeviceservice.command.entity.Command

object ExternalCommandFactory {
    fun externalRebootCommand() =
        com.alliander.sng.Command.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestamp(timestamp)
            .setCommand(Command.CommandType.REBOOT.name)
            .setValue(null)
            .build()!!

    fun externalRebootCommandInvalid() =
        com.alliander.sng.Command.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestamp(timestamp)
            .setCommand("unknown")
            .setValue(null)
            .build()!!
}
