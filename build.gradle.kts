// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.springBoot) apply false
    alias(libs.plugins.dependencyManagement) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.spring) apply false
    alias(libs.plugins.jpa) apply false
    alias(libs.plugins.avro) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.eclipse)
    alias(libs.plugins.gradleWrapperUpgrade)
}

version = System.getenv("GITHUB_REF_NAME")?.replace("/", "-")?.lowercase() ?: "develop"

wrapperUpgrade {
    gradle {
        register("sng-crest-device-service") {
            repo.set("OSGP/sng-crest-device-service")
            baseBranch.set("main")
        }
    }
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "OSGP_sng-crest-device-service")
        property("sonar.organization", "gxf")
        property("sonar.gradle.skipCompile", true)
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)
    apply(plugin = rootProject.libs.plugins.spring.get().pluginId)
    apply(plugin = rootProject.libs.plugins.dependencyManagement.get().pluginId)
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    apply(plugin = rootProject.libs.plugins.eclipse.get().pluginId)
    apply(plugin = rootProject.libs.plugins.jpa.get().pluginId)
    apply(plugin = rootProject.libs.plugins.jacoco.get().pluginId)
    apply(plugin = rootProject.libs.plugins.jacocoReportAggregation.get().pluginId)

    group = "org.gxf.deviceservice"
    version = rootProject.version

    repositories {
        mavenCentral()
        maven {
            name = "GXFGithubPackages"
            url = uri("https://maven.pkg.github.com/osgp/*")
            credentials {
                username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    extensions.configure<SpotlessExtension> {
        kotlin {
            ktlint().editorConfigOverride(
                mapOf(
                    "max_line_length" to "120",
                    "ij_kotlin_indent_before_arrow_on_new_line" to true
                )
            )

            licenseHeaderFile("${project.rootDir}/license-template.kt", "package").updateYearWithLatest(false)
        }
    }

    extensions.configure<StandardDependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            mavenBom(rootProject.libs.springBootDependencies.get().toString()) {
                bomProperty("kotlin.version", rootProject.libs.plugins.kotlin.get().version.toString())
            }
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.register<Copy>("updateGitHooks") {
        description = "Copies the pre-commit Git Hook to the .git/hooks folder."
        group = "verification"
        from("${project.rootDir}/scripts/pre-commit")
        into("${project.rootDir}/.git/hooks")
    }

    tasks.withType<KotlinCompile> {
        dependsOn(
            tasks.withType<GenerateAvroJavaTask>(),
            tasks.named("updateGitHooks")
        )
    }

    tasks.withType<Test> { useJUnitPlatform() }
}
