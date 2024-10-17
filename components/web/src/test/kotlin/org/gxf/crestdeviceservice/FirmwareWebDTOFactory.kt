// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.web.FirmwareWebDTO

object FirmwareWebDTOFactory {
    fun getFirmwareWebDTO() = FirmwareWebDTO("name", listOf())
}
