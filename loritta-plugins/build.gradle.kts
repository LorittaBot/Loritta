dependencies {
    api(project(":loritta-discord"))
}

plugins {
    java
    kotlin("jvm")
    `maven-publish`
    kotlin("plugin.serialization") version "1.4.10"
}