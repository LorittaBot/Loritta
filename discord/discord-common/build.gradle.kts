plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "net.perfectdreams.loritta.cinnamon.discord"

dependencies {
    api(project(":common"))
    api(project(":pudding:client"))

    api(libs.kotlinx.coroutines.core)

    // Discord InteraKTions my beloved
    // We only depend in the common module here, the interactions/gateway will have the proper Discord InteraKTions modules related to them.
    api("net.perfectdreams.discordinteraktions:common:${Versions.DISCORD_INTERAKTIONS}")

    // Used to serialize state on components
    implementation("io.github.netvl.ecoji:ecoji:1.0.0")

    // We want to use Kord REST on our project too!
    api("dev.kord:kord-rest:${Versions.KORD}")

    // Databases
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)

    // Logback GELF, used for Graylog logging
    implementation("de.siegmar:logback-gelf:3.0.0")

    api("org.jsoup:jsoup:1.13.1")
    api("net.perfectdreams.randomroleplaypictures:client:1.0.1")

    testImplementation(libs.logback.classic)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
}

tasks.test {
    useJUnitPlatform()
}