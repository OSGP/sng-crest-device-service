// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk

import com.alliander.sng.DeviceCredentials
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class IncomingDeviceCredentialsService(
    private val pskService: PskService,
    private val pskDecryptionService: PskDecryptionService
) {

    private val logger = KotlinLogging.logger {}

    @KafkaListener(
        topics = ["\${crest-device-service.kafka.pre-shared-key-consumer.topic-name}"],
        id = "\${crest-device-service.kafka.pre-shared-key-consumer.id}")
    fun handleIncomingDeviceCredentials(deviceCredentials: DeviceCredentials) {
        logger.info { "Received key for ${deviceCredentials.imei}" }

        val identity = deviceCredentials.imei

        try {
            val decryptedPsk =
                pskDecryptionService.decryptSecret(deviceCredentials.psk, deviceCredentials.keyRef)
            val decryptedSecret =
                pskDecryptionService.decryptSecret(
                    deviceCredentials.secret, deviceCredentials.keyRef)

            pskService.setInitialKeyForIdentity(identity, decryptedPsk, decryptedSecret)

            logger.info { "Creating new ready key for device $deviceCredentials.imei" }
            pskService.generateNewReadyKeyForIdentity(identity)
        } catch (e: Exception) {
            logger.error(e) { "Failed to set device credentials for $identity" }
        }
    }
}
