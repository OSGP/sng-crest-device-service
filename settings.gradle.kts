// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-service"

include("application")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql", "postgresql").withoutVersion()
            library("flyway", "org.flywaydb", "flyway-core").withoutVersion()
            bundle("data", listOf("postgresql", "flyway"))

            library("logging", "io.github.oshai", "kotlin-logging-jvm").version("6.0.3")

            library("avro", "org.apache.avro", "avro").version("1.11.3")

            version("utilities", "0.3.7")
            library("kafkaAvro", "com.gxf.utilities", "kafka-avro").versionRef("utilities")
            library("kafkaAzureOauth", "com.gxf.utilities", "kafka-azure-oauth").versionRef("utilities")
            bundle("gxfUtils", listOf("kafkaAvro", "kafkaAzureOauth"))

            library("mockitoKotlin", "org.mockito.kotlin", "mockito-kotlin").version("5.1.0")

            library("commonsCodec", "commons-codec", "commons-codec").version("1.16.0")
        }
        create("integrationTestLibs") {
            library("h2", "com.h2database", "h2").version("2.2.224")
            library("kafkaTestContainers", "org.testcontainers", "kafka").version("1.19.7")
        }
    }
}
