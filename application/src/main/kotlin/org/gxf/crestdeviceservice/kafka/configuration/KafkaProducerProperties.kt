// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("crest-device-service.kafka.message-producer")
class KafkaProducerProperties(val topicName: String)
