plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "net.perfectdreams.loritta.cinnamon.discord"

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
                api(project(":common"))

                // Discord InteraKTions my beloved
                // We only depend in the common module here, the interactions/gateway will have the proper Discord InteraKTions modules related to them.
                api("net.perfectdreams.discordinteraktions:common:0.0.8-kord-yay-SNAPSHOT")

                // We want to use Kord REST!
                api("dev.kord:kord-rest:0.8.x-SNAPSHOT")

                // Kord bugs
                implementation("io.ktor:ktor-client-cio:1.6.0")

                // Prometheus
                api("io.prometheus:simpleclient:0.10.0")
                api("io.prometheus:simpleclient_hotspot:0.10.0")
                api("io.prometheus:simpleclient_common:0.10.0")
            }
        }
    }
}