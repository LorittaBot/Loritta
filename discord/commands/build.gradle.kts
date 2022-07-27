plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":discord:discord-common"))

    // Discord InteraKTions my beloved
    // We only depend common here, because we use it for command registration.
    api("net.perfectdreams.discordinteraktions:common:${Versions.DISCORD_INTERAKTIONS}")

    // Kord REST
    api("dev.kord:kord-rest:${Versions.KORD}")
}