plugins {
    kotlin("multiplatform") version Versions.KOTLIN
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

                // Sequins
                api("net.perfectdreams.sequins.ktor:base-route:1.0.2")

                // Base Route uses a more up to date impl of Ktor compared to Discord InteraKTions
                // (because Kord Core does not work with Ktor 1.5.X yet)
                api("io.ktor:ktor-server-core:1.4.1")
                api("io.ktor:ktor-server-netty:1.4.1")

                implementation("net.perfectdreams.discordinteraktions:core:0.0.4-SNAPSHOT")

                // Prometheus
                api("io.prometheus:simpleclient:0.10.0")
                api("io.prometheus:simpleclient_hotspot:0.10.0")
                api("io.prometheus:simpleclient_common:0.10.0")
            }
        }
    }
}