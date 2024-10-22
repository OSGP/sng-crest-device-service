// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.exception

class DuplicateDeviceException(id: String) : Exception("Duplicate device: $id")
