dependencies {
    api(project(":loritta-discord"))
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version Versions.KOTLIN
}