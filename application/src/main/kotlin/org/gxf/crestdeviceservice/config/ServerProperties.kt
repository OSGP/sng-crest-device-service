// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("server") class ServerProperties(val port: Int)
