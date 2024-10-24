dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
