plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Ktor
    implementation(libs.ktor.client.java)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)
    // https://mvnrepository.com/artifact/com.upokecenter/cbor
    implementation("com.upokecenter:cbor:5.0.0-alpha2")

    // Logging Stuff
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    // Used for logs - MojangStyleFileAppenderAndRollover
    implementation("com.github.luben:zstd-jni:1.5.5-6")
}