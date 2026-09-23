plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"
}

group = "streamix"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("io.ktor:ktor-server-core-jvm:3.3.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.3.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.1")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("com.google.http-client:google-http-client-apache-v2:1.47.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.47.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.3.1")
}

tasks.test {
    useJUnitPlatform()
}
