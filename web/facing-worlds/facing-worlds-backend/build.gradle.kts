plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(project(":web:facing-worlds:facing-worlds-common"))
    implementation(project(":pudding:client"))

    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.KOTLIN_COROUTINES}")

    // Exposed & Databases
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.javatime)
    api(libs.exposed.dao)
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.2.1")

    // Prometheus
    implementation("io.prometheus:simpleclient:0.9.0")
    implementation("io.prometheus:simpleclient_hotspot:0.9.0")
    implementation("io.prometheus:simpleclient_common:0.9.0")

    // Logging
    runtimeOnly(libs.logback.classic)
}