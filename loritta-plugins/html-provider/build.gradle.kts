dependencies {
    api(project(":loritta-discord"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.10"
    `maven-publish`
}