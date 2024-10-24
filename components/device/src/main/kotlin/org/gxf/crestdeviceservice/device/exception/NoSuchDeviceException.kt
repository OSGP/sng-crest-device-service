// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.exception

class NoSuchDeviceException(id: String) : Exception("No such device: $id")
