plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

group = "net.perfectdreams.randomroleplaypictures"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8"))

    // Logging Stuff
    implementation(libs.kotlin.logging)
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha14")

    // Ktor
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")

    // Sequins
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/random-roleplay-pictures-backend"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "openjdk:17-slim-bullseye"
    }
}