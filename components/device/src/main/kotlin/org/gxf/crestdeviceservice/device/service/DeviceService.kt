// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.service

import kotlin.jvm.optionals.getOrNull
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.exception.DuplicateDeviceException
import org.gxf.crestdeviceservice.device.exception.NoSuchDeviceException
import org.gxf.crestdeviceservice.device.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceService(private val repository: DeviceRepository) {
    fun createDevice(id: String, secret: String): Device {
        if (repository.existsById(id)) {
            throw DuplicateDeviceException(id)
        } else {
            return repository.save(Device(id, secret))
        }
    }

    fun getDevice(id: String) = repository.findById(id).getOrNull() ?: throw NoSuchDeviceException(id)
}
