// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.HASH_LENGTH_BYTES
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.OTA_COMMAND_LENGTH
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket.Companion.OTA_DONE
import org.gxf.utilities.Base85
import org.springframework.stereotype.Service

@OptIn(ExperimentalStdlibApi::class)
@Service
class FirmwareHashService {
    private val logger = KotlinLogging.logger {}

    /**
     * Generates a device specific packet. This means that the firmware hashes at the start and end of the firmware are
     * replaced with a new hash over the device's secret and the original hash. See Crest specs for details.
     *
     * @param firmwarePacket the original packet (as an entity)
     * @param deviceSecret the secret with which to hash the firmware hash(es)
     * @return the downlink command ready to be sent to the device
     */
    fun generateDeviceSpecificPacket(firmwarePacket: FirmwarePacket, deviceSecret: String): String {
        if (!firmwarePacket.isLastPacket() && !firmwarePacket.isLastPacket()) {
            return firmwarePacket.packet
        }

        var firmwareBytes = getFirmwareBytes(firmwarePacket)

        if (firmwarePacket.isFirstPacket()) {
            firmwareBytes = makeCurrentFirmwareHashDeviceSpecific(firmwareBytes, deviceSecret)
        }
        if (firmwarePacket.isLastPacket()) {
            firmwareBytes = makeTargetFirmwareHashDeviceSpecific(firmwareBytes, deviceSecret)
        }
        return reAssemble(firmwarePacket, firmwareBytes)
    }

    /**
     * Decode the Base85 content in the firmware packet to a ByteArray
     *
     * @return A ByteArray, decoded from the Base85 portion of the firmware packet
     */
    private fun getFirmwareBytes(firmwarePacket: FirmwarePacket): ByteArray {
        var base85String = firmwarePacket.packet.drop(OTA_COMMAND_LENGTH)
        if (firmwarePacket.isLastPacket()) {
            base85String = base85String.dropLast(OTA_DONE.length)
        }
        return Base85.getRfc1924Decoder().decodeToBytes(base85String)
    }

    /**
     * Replace the Base85 content of the packet with the newly hashed bytes, preserving the command (`OTA####`) and
     * optional ending (`:DONE`)
     *
     * @return the updated firmware packet
     */
    private fun reAssemble(firmwarePacket: FirmwarePacket, firmwareBytes: ByteArray): String {
        val command = firmwarePacket.packet.take(OTA_COMMAND_LENGTH)
        val base85 = Base85.getRfc1924Encoder().encodeToString(firmwareBytes)
        val endString = if (firmwarePacket.isLastPacket()) OTA_DONE else ""

        return "$command$base85$endString"
    }

    /**
     * "Sign" the current firmware hash with the device secret to create a device-specific, verifiable hash.
     *
     * The first 32 bytes of the Base85 payload contain the hash of the firmware that should be present (current) on the
     * device. The device secret and this current hash are re-hashed These rehashed bytes replace the first 32 bytes of
     * the Base85 payload
     */
    private fun makeCurrentFirmwareHashDeviceSpecific(firmwareBytes: ByteArray, deviceSecret: String): ByteArray {
        logger.info { "=== CURRENT HASH ===" }

        val currentFirmwareHash = firmwareBytes.take(HASH_LENGTH_BYTES).toByteArray()
        logger.debug { "Current firmware hash: ${currentFirmwareHash.toHexString(HexFormat.Default)}" }

        val deviceSpecificHash = calculateHash(deviceSecret, currentFirmwareHash)
        logger.debug { "Device specific current hash: ${deviceSpecificHash.toHexString(HexFormat.Default)}" }

        return deviceSpecificHash + firmwareBytes.drop(HASH_LENGTH_BYTES).toByteArray()
    }

    /**
     * "Sign" the target firmware hash with the device secret to create a device-specific, verifiable hash.
     *
     * The last 32 bytes of the Base85 payload contain the hash of the firmware that is being sent (target) to the
     * device. The device secret and this current hash are re-hashed These rehashed bytes replace the last 32 bytes of
     * the Base85 payload
     */
    private fun makeTargetFirmwareHashDeviceSpecific(firmwareBytes: ByteArray, deviceSecret: String): ByteArray {
        logger.info { "=== TARGET HASH ===" }

        val targetFirmwareHash = firmwareBytes.takeLast(HASH_LENGTH_BYTES).toByteArray()
        logger.debug { "Target firmware hash: ${targetFirmwareHash.toHexString(HexFormat.Default)}" }

        val deviceSpecificHash = calculateHash(deviceSecret, targetFirmwareHash)
        logger.debug { "Device specific target hash: ${deviceSpecificHash.toHexString(HexFormat.Default)}" }

        return firmwareBytes.take(firmwareBytes.size - HASH_LENGTH_BYTES).toByteArray() + deviceSpecificHash
    }

    private fun calculateHash(deviceSecret: String, byteHash: ByteArray): ByteArray =
        DigestUtils.sha256(deviceSecret.toByteArray().plus(byteHash))
}
