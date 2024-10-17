// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-service"

include("application")
include("components:avro")
include("components:psk")
include("components:firmware")
include("components:web")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinLogging", "7.0.0")
            version("avro", "1.12.0")
            version("mockitoKotlin", "5.4.0")
            version("commonsCodec", "1.17.1")
            version("gxfUtils", "2.0")

            library("logging", "io.github.oshai", "kotlin-logging-jvm").versionRef("kotlinLogging")

            library("avro", "org.apache.avro", "avro").versionRef("avro")

            library("kafkaAvro", "com.gxf.utilities", "kafka-avro").versionRef("gxfUtils")
            library("kafkaAzureOauth", "com.gxf.utilities", "kafka-azure-oauth").versionRef("gxfUtils")
            bundle("gxfUtils", listOf("kafkaAvro", "kafkaAzureOauth"))

            library("mockitoKotlin", "org.mockito.kotlin", "mockito-kotlin").versionRef("mockitoKotlin")

            library("commonsCodec", "commons-codec", "commons-codec").versionRef("commonsCodec")
        }
    }
}
