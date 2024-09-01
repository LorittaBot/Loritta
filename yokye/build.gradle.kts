plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Ktor
    implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.7.2")
    // https://mvnrepository.com/artifact/com.upokecenter/cbor
    implementation("com.upokecenter:cbor:5.0.0-alpha2")

    // Logging Stuff
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    // Used for logs - MojangStyleFileAppenderAndRollover
    implementation("com.github.luben:zstd-jni:1.5.5-6")
}