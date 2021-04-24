import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    maven("https://repo.perfectdreams.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
                implementation(project(":commands"))
                implementation(project(":discord:common"))
                implementation(project(":discord:commands"))

                implementation("net.perfectdreams.discordinteraktions:core:0.0.4-SNAPSHOT")

                // Sequins
                api("net.perfectdreams.sequins.ktor:base-route:1.0.2")

                // Prometheus
                api("io.prometheus:simpleclient:0.10.0")
                api("io.prometheus:simpleclient_hotspot:0.10.0")
                api("io.prometheus:simpleclient_common:0.10.0")
            }
        }
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation("dev.kord:kord-rest:0.7.x-SNAPSHOT")
            }
        }
    }
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTionsLauncher"
    }
}