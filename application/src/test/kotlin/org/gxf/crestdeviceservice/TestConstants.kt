// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.time.Instant
import java.util.UUID

object TestConstants {
    const val DEVICE_ID = "device-id"
    const val MESSAGE_RECEIVED = "Command received"
    val CORRELATION_ID = UUID.randomUUID()
    val timestamp = Instant.now()
    const val FIRMWARE_VERSION = "99.99"
    const val FIRMWARE_FROM_VERSION = "23.10"
    const val FIRMWARE_NAME = "RTU#DELTA#FROM#$FIRMWARE_FROM_VERSION#TO#$FIRMWARE_VERSION"
    const val NUMBER_OF_PACKETS = 13
    const val DEVICE_MESSAGE_TOPIC = "device-message"
    const val COMMAND_FEEDBACK_TOPIC = "command-feedback"
    const val FIRMWARE_TOPIC = "firmware-topic"
    const val FIRMWARE_PACKET_0 =
        "OTA0000or^kx?6<yS=?v_sLE%07c5\$Q44bK-ZDiN~L0#M3`5CO0Nu?N}rC*rK{yx{@B9g?g)9S{gm0NN+;k=rLQmqGAk?RVnN0izHIpa8sqK#|lZP?6OqV3F1*aFKHm2(SRxCxjc73#JRV3yFi8gR6rv5D5SP&9~mSYyo=#;E~!V@R8dm@DK?Q05F#+@MP_G;t7?E0Rq4e?_v-MAOIil_5nbFQGsHCbAf=7)F(_32`~VVk<}-lk=7@\$k=G}L50xM05Dee|54I1I5hRb3hTwpc2`7({eh>`+01uK8B#)DylY_B=aFCG>Cq57j5CD-81;B_S8Ss(dkdq82fRo-34IlsxC!djzkq?j\$z7LQekQkNM5Dh>8g8PUkfccP-4=0%;De!?2KoAX300Y32;E<CICxDX=C!djzk!=tSkN|11X8UF*VEIg&2Ec%k3MT^)4WIyl5e&eD;0M4>n+U*wkqRfs5Dl;Zfe{42h2Vgb3MVO=K#?XVQxFZn0EOb0;trHkGXIh9k-w3zk\$eykPym3D3MPRO2f\$yL5Rw%Ff#Bs35O4s3;DD0~CMuXKm?@Z9nt+jk5D<_6fRPC&fe{wK4wQEikl=uo_Yfrz07uV&;0Tp~k!L4{m0Zn;4=xZTAOL`qXD48jX(vFFXeS7j|C09*B|rcOFYu6PClHZoC*s_J5fH%i5G7CmfRSe>1KoiU3&0;K5NIIc-4G>!0AS#7;L8Cdz<`lwCy!s|+`JGaumB<VfiH*;hzpE^kdbI7oG_jcCGY^ik!UBzmBy70lw4eE0ek@h5GDWsfRSk@lHkdofDI8OVc>A!{17tG0DzGcCy<d9C!moQC\$NzhCs+_N-~fp\$iYtpNA@v}V6elo}6(_zBGVlOEk`^aWk{2f%z%byD-~pGc5Hx@Q+LRBF50KFi)6amBA}4_nArLlj0C@volXwGwks>FEBQfxU;MWj25CA3MHsV9=THs;ecHj<_c?x6@Ij{iUw>*g8ksBuKFbME\$z\$XEf5IN8Q>0J6vAcHZ3LW5I-+yU&74*"
    const val FIRMWARE_PACKET_1 =
        "OTA00015INugu;J^U@BtAd>0J6v6oM6k-4Hqe0O?%%O+bcGhGK?uhTH+{kx~#k5CE{@>!0ue5hUqc`c0&UwT5&MIxqmh0TCoj;85U\$UYSh}l!IpC5IR5rC*lS&5(g>tF%T8N;E^ULD-b\$R00_W`;si1n2XXW<5DdWJk^K-lU;rd1@Zjzb{~zU1`b~r2kl@e|I)DJ>QTk1hf`Ed6ksBtDUx1TQ5IT?m8zz9@0l<Oa-;vLeACV7{Ll8Rf0PGQf;0}~`5)hIJCxGAxm2VI`5C8y|S@aN+87740;p+C{-vRRwJ0JiZl8Tk-_krL(z`&6iCdx4W5IZmcxb=(yf\$HckhT`}x(BRD%s1Q4F0K%WZ0TCpRRhdnnlaG@Qlr9iEfB->ffRPy{k6(b3877Ulp0{KWJP-gRC4u1R8u17N=o8=qzyj!X5Iv9p{?CKrkl+rKGiZR51tyS_;}AWd00t%%G12fq^bs(b@M!Q9F+~tQZ~%xS5%6H+aNuB*947gh{?Bg^K9B\$\$CV=3O;0}~#Xn>I%CS%\$V5I&#)fRP*~ACixgh2@*zQpb+vdk{eI01lK5WcDtAksBuX76|Y|!7UI\$pa6&?3G~~=8Nh&%CMWn31;D`&L9hVsAPMx&z=Gftz<`k?C-@e}5JE5jiCmgZAmQmP_\$}N40gw%nX%IqS0QdpvT>4Gl0UVYSmW+`Tm&Xu7@Bn&=g#iE\$5g+&g<x%=gaf6HyLl6KClv!x_0p(HpO@V=rlYoJr5JYeQg5ZFY3MPZ?oZtn(f#86Xau7s-0176H?VR8~+fl?q;85UV?l=%czyJ`EC?^n-Cnpb(50Lqp{?Bd@MqmH|z>a@_lN=}cnf}jUksK\$J5Jw;Yf#86XB_)89C?\$ZCDJ6iD_Yg-g0461XlPV>ElP4vBlPo2GllBltKmaQxfRisJfRimHfRipIE5Q5^M_>SO;K%_ElnrEnksBsx7clfF5JzwTf#86XB_&{!F(ur<hv=N(s}M(^0D<6ulO-j9lQ1QKksBs}lYR"
}
