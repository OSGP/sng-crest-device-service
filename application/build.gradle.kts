import org.springframework.boot.gradle.tasks.bundling.BootJar

// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.springBoot)
    alias(libs.plugins.avro)
}

dependencies {
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springBootStarterThymeleaf)
    implementation(libs.springSecurityCore)
    implementation(libs.springKafka)

    implementation(project(":components:avro"))
    implementation(project(":components:device"))
    implementation(project(":components:firmware"))
    implementation(project(":components:psk"))

    implementation(libs.kotlinReflect)
    implementation(libs.logging)

    implementation(libs.bundles.gxfUtils)

    implementation(libs.commonsCodec)

    implementation(libs.jacksonKotlinModule)

    runtimeOnly(libs.micrometerPrometheusModule)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway)

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)

    testRuntimeOnly(libs.junitPlatformLauncher)

    // Generate test and integration test reports
    jacocoAggregation(project(":application"))
}

tasks.withType<BootJar> {
    // Exclude test keys and certificates
    exclude("ssl/*.pem")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName.set("ghcr.io/osgp/gxf-sng-crest-device-service:${version}")
    environment.set(
        mapOf(
            "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
            "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-Djava.security.egd=file:/dev/urandom"
        )
    )

    if (project.hasProperty("publishImage")) {
        publish.set(true)
        docker {
            publishRegistry {
                username.set(System.getenv("GITHUB_ACTOR"))
                password.set(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(project(":components:avro"))
                implementation(project(":components:psk"))
                implementation(project(":components:device"))
                implementation(project(":components:firmware"))
                implementation(libs.springBootStarterDataJpa)
                implementation(libs.kafkaAvro)
                implementation(libs.springKafka)
                implementation(libs.springBootStarterTest)
                implementation(libs.springKafkaTest)
                implementation(libs.kafkaTestContainer)
                implementation(libs.springWsTest)
                implementation(libs.mockk)
                runtimeOnly(libs.h2)
            }
        }
    }
}
