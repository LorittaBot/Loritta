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
        val jvmMain by getting {
            dependencies {
                api(project(":common"))
                api(project(":pudding:client"))

                // Discord InteraKTions my beloved
                // We only depend in the common module here, the interactions/gateway will have the proper Discord InteraKTions modules related to them.
                api("net.perfectdreams.discordinteraktions:common:${Versions.DISCORD_INTERAKTIONS}")

                // Used to serialize state on components
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.KOTLINX_SERIALIZATION}")
                // Used to serialize state on components
                implementation("io.github.netvl.ecoji:ecoji:1.0.0")

                // We want to use Kord REST on our project too!
                api("dev.kord:kord-rest:${Versions.KORD}")

                // Prometheus, for metrics
                api("io.prometheus:simpleclient:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_hotspot:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_common:${Versions.PROMETHEUS}")

                // Logback GELF, used for Graylog logging
                implementation("de.siegmar:logback-gelf:3.0.0")
            }
        }
    }
}