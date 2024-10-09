// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service.command

import org.gxf.crestdeviceservice.command.entity.Command

/** Generates a custom String command to be sent to a Crest device */
interface CommandGenerator {
    /** Returns the CommandType this generator can handle */
    fun getSupportedCommand(): Command.CommandType

    /** Returns the command including extra data needed by the device */
    fun generateCommandString(command: Command): String
}
