// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.HASH_LENGTH
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.OTA_DONE
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.OTA_START
import org.gxf.utilities.Base85
import org.springframework.stereotype.Service

interface FirmwareHashService {
    /**
     * Generates a device specific packet. This means that the firmware hashes at the start and end of the firmware are
     * replaced with a new hash over the device's secret and the original hash. See Crest specs for details.
     *
     * @param firmwarePacket the original packet (as an entity)
     * @param deviceSecret the secret with which to hash the firmware hash(es)
     * @return the downlink command ready to be sent to the device
     */
    fun generateDeviceSpecificPacket(firmwarePacket: FirmwarePacket, deviceSecret: String): String
}

@Service
internal class InternalFirmwareHashService : FirmwareHashService {
    private val logger = KotlinLogging.logger {}

    /**
     * Generates a device specific packet. This means that the firmware hashes at the start and end of the firmware are
     * replaced with a new hash over the device's secret and the original hash. See Crest specs for details.
     *
     * @param firmwarePacket the original packet (as an entity)
     * @param deviceSecret the secret with which to hash the firmware hash(es)
     * @return the downlink command ready to be sent to the device
     */
    override fun generateDeviceSpecificPacket(firmwarePacket: FirmwarePacket, deviceSecret: String): String {
        var result = firmwarePacket.packet
        if (firmwarePacket.isFirstPacket()) {
            result = replaceCurrentFirmwareHash(result, deviceSecret)
        }
        if (firmwarePacket.isLastPacket()) {
            result = replaceTargetFirmwareHash(result, deviceSecret)
        }
        return result
    }

    private fun replaceCurrentFirmwareHash(packet: String, deviceSecret: String): String {
        val command = packet.substring(0, OTA_START.length)
        val currentFirmwareHash = packet.substring(OTA_START.length, OTA_START.length + HASH_LENGTH)
        val theRest = packet.substring(OTA_START.length + HASH_LENGTH, packet.length)

        logger.info { "=== START HASH ===" }
        val base85Hash = calculateHash(deviceSecret, currentFirmwareHash)

        return "$command$base85Hash$theRest"
    }

    private fun replaceTargetFirmwareHash(packet: String, deviceSecret: String): String {
        val packetLength = packet.length
        val theStart = packet.substring(0, packetLength - HASH_LENGTH - OTA_DONE.length)
        val targetFirmwareHash =
            packet.substring(packetLength - HASH_LENGTH - OTA_DONE.length, packetLength - OTA_DONE.length)

        logger.info { "=== END HASH ===" }
        val base85Hash = calculateHash(deviceSecret, targetFirmwareHash)

        return "$theStart$base85Hash:DONE"
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun calculateHash(deviceSecret: String, stringHash: String): String? {
        val byteHash = Base85.getRfc1924Decoder().decodeToBytes(stringHash)
        logger.info { "firmware hash (Base85 -> Hex): $stringHash -> ${byteHash.toHexString(HexFormat.UpperCase)}" }
        val deviceSpecificHash = DigestUtils.sha256(deviceSecret.toByteArray().plus(byteHash))
        logger.info { "sha56 over secret + hash: ${deviceSpecificHash.toHexString(HexFormat.UpperCase)}" }
        val base85Hash = Base85.getRfc1924Encoder().encodeToString(deviceSpecificHash)
        logger.info { "device specific hash encoded to Base85: $base85Hash" }
        return base85Hash
    }
}
