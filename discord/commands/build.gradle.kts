plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    sourceSets {
        // jvmMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(project(":discord:common"))
                implementation("org.jsoup:jsoup:1.13.1")

                // Discord InteraKTions my beloved
                // We only depend in the Common Kord here, because we use it for command registration.
                api("net.perfectdreams.discordinteraktions:common-kord:0.0.8-kord-yay-SNAPSHOT")

                // Kord REST
                api("dev.kord:kord-rest:0.8.x-SNAPSHOT")
            }
        }
    }
}