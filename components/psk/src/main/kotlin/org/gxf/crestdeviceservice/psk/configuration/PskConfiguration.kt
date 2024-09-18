// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @property changeInitialPsk
 */
@ConfigurationProperties("psk") class PskConfiguration(val changeInitialPsk: Boolean = true)
