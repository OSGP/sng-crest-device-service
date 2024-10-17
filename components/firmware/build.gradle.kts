plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.kafka:spring-kafka")

    implementation(libs.logging)

    implementation(project(":components:avro"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
