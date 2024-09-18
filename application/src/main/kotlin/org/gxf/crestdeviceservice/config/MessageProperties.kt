// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @property maxBytes
 */
@ConfigurationProperties("message") class MessageProperties(val maxBytes: Int)
