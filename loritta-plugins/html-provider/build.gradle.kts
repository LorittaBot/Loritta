dependencies {
    api(project(":platforms:discord:legacy"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}