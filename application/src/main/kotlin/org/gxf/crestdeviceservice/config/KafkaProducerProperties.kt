// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @property deviceMessage
 * @property commandFeedback
 */
@ConfigurationProperties("kafka.producers")
class KafkaProducerProperties(
    val deviceMessage: KafkaProducerTopicProperties,
    val commandFeedback: KafkaProducerTopicProperties
)
