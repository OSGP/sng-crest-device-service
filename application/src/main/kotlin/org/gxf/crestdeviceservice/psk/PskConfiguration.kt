// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.psk

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("crest-device-service.psk")
class PskConfiguration(val changeInitialPsk: Boolean = true)
