dependencies {
    api(project(":loritta-discord"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.3.70"
    `maven-publish`
}