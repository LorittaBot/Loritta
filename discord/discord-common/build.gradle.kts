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

                // Databases
                implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
                implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
                implementation("org.postgresql:postgresql:42.2.23")
                implementation("org.xerial:sqlite-jdbc:3.36.0.3")
                implementation("com.zaxxer:HikariCP:5.0.0")

                // Used to serialize state on components
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.KOTLINX_SERIALIZATION}")
                // Used to serialize state on components
                implementation("io.github.netvl.ecoji:ecoji:1.0.0")

                // We want to use Kord REST on our project too!
                api("dev.kord:kord-rest:0.8.x-SNAPSHOT")

                // Prometheus, for metrics
                api("io.prometheus:simpleclient:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_hotspot:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_common:${Versions.PROMETHEUS}")
            }
        }
    }
}