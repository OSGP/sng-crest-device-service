// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-service"

include("application")
include(":components:avro-measurement")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql", "postgresql").version("42.5.4")
            library("flyway", "org.flywaydb", "flyway-core").version("9.22.3")
            bundle("data", listOf("postgresql", "flyway"))

            library("logging", "io.github.microutils", "kotlin-logging-jvm").version("3.0.5")

            version("utilities", "0.2")
            library("kafkaAvro", "com.gxf.utilities", "kafka-avro").versionRef("utilities")
            library("kafkaAzureOauth", "com.gxf.utilities", "kafka-azure-oauth").versionRef("utilities")
            bundle("gxfUtils", listOf("kafkaAvro", "kafkaAzureOauth"))

            library("mockitoKotlin", "org.mockito.kotlin", "mockito-kotlin").version("5.1.0")

            library("microsoftMsal", "com.microsoft.azure", "msal4j").version("1.13.10")
        }
        create("integrationTestLibs") {
            library("h2", "com.h2database", "h2").version("2.2.224")
            library("kafkaTestContainers", "org.testcontainers", "kafka").version("1.17.6")
        }
    }
}
