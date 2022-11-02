import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version libs.versions.jib
    kotlin("plugin.serialization") version Versions.KOTLIN
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

dependencies {
    implementation(project(":common"))
    implementation(project(":devious-cache:devious-data"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.logback.classic)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-jetty:${Versions.KTOR}")
    implementation("io.ktor:ktor-websockets:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-java:${Versions.KTOR}")
    implementation("io.ktor:ktor-network-tls-certificates:${Versions.KTOR}")
    implementation("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING}")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")
    implementation("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation("net.perfectdreams.exposedpowerutils:exposed-power-utils:1.1.0")

    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    implementation("it.unimi.dsi:fastutil:8.5.9")

    // zstd
    implementation("com.github.luben:zstd-jni:1.5.2-4")

    implementation("dev.kord:kord-common") {
        version {
            strictly("0.8.x-lori-fork-20221014.000427-14")
        }
    }
}

jib {
    to {
        image = "ghcr.io/lorittabot/devious-cache"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:18-focal"
    }
}

tasks.test {
    useJUnitPlatform()
}