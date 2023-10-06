// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("org.springframework:spring-aspects")
    implementation("org.springframework:spring-aop")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.microsoft.azure:msal4j:1.13.10")

    testImplementation("org.springframework:spring-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}
