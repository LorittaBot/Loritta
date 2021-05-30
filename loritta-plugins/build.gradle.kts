dependencies {
    api(project(":platforms:discord:legacy"))
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version Versions.KOTLIN
}