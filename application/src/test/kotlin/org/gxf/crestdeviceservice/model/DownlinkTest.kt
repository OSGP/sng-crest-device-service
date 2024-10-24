// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DownlinkTest {
    @Test
    fun firstCommandFitsMaxMessageSize() {
        val downlink = Downlink()
        val downlinkToAdd = "CMD:REBOOT"

        val result = downlink.addIfPossible(downlinkToAdd)

        assertThat(result).isTrue()
        assertThat(downlink.getDownlink()).isEqualTo("!$downlinkToAdd")
    }

    @Test
    fun multipleCommandsFitMaxMessageSize() {
        val downlink = Downlink()
        val downlinkExisting = "CMD:REBOOT;CMD:REBOOT"
        val downlinkToAdd = "CMD:REBOOT"

        val existingFits = downlink.addIfPossible(downlinkExisting)
        val result = downlink.addIfPossible(downlinkToAdd)

        assertThat(existingFits).isTrue()
        assertThat(result).isTrue()
        assertThat(downlink.getDownlink()).isEqualTo("!$downlinkExisting;$downlinkToAdd")
    }

    @Test
    fun shouldBlockWhenFirmwareIsPresent() {
        val downlink = Downlink()
        val expectedDownlink = "OTA0000:small-enough-to-have-room-for-other-commands"
        downlink.addIfPossible(expectedDownlink)

        assertThat(downlink.getDownlink()).isEqualTo("!$expectedDownlink")
        assertThat(downlink.addIfPossible("CMD:REBOOT")).isFalse()
        assertThat(downlink.getDownlink()).isEqualTo("!$expectedDownlink")
    }

    @Test
    fun shouldBlockFirmwareIfOtherCommandIsPresent() {
        val downlink = Downlink()
        val rebootCommand = "CMD:REBOOT"
        val rspCommand = "CMD:RSP"

        assertThat(downlink.addIfPossible(rebootCommand)).isTrue()
        assertThat(downlink.addIfPossible("OTA0000:please-don't-combine-with-other-commands")).isFalse()
        assertThat(downlink.addIfPossible(rspCommand)).isTrue()

        assertThat(downlink.getDownlink()).isEqualTo("!$rebootCommand;$rspCommand")
    }

    @Test
    fun doesNotFitMaxMessageSize() {
        val downlink = Downlink()
        val downlinkExisting =
            "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071;" +
                "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071:SET;" +
                "OTA0000^IO>dUJt\"`!&`;3d5CdvyU6^v1Kn)OEu?2GK\"yK\"5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#I{;" +
                "OTA004E5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#JM&xE{\$3Bi&BI%Tvw4VdaJ\$I-w\"%d5\$}Oa4EI`MX`T:DONE;" +
                "OTA0000^IO>dUJt\"`!&`;3d5CdvyU6^v1Kn)OEu?2GK\"yK\"5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#I{;" +
                "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071:SET;" +
                "OTA004E5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#JM&xE{\$3Bi&BI%Tvw4VdaJ\$I-w\"%d5\$}Oa4EI`MX`T:DONE;" +
                "CMD:REBOOT;" +
                "CMD:REBOOT"
        val downlinkToAdd = "CMD:REBOOT"

        val existingFits = downlink.addIfPossible(downlinkExisting)
        val added = downlink.addIfPossible(downlinkToAdd)

        assertThat(existingFits).isTrue()
        assertThat(added).isFalse()
        assertThat(downlink.getDownlink()).isEqualTo("!$downlinkExisting")
    }

    @Test
    fun shouldReturnDefaultDownlinkWhenNoCommandGiven() {
        assertThat(Downlink().getDownlink()).isEqualTo(Downlink.RESPONSE_SUCCESS)
    }
}
