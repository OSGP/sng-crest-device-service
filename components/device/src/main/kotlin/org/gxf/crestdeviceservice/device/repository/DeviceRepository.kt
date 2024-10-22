// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.repository

import org.gxf.crestdeviceservice.device.entity.Device
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository interface DeviceRepository : CrudRepository<Device, String>
