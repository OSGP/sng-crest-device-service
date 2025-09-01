plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springKafka)
    implementation(libs.springBootStarterWeb)

    implementation(project(":components:device"))
    implementation(project(":components:psk"))
    implementation(project(":components:base85"))

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    implementation(project(":components:avro"))

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
