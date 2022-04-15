import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(project(":common"))
    implementation(project(":discord:discord-common"))
    implementation(project(":discord:commands"))

    // Discord InteraKTions (Web Server)
    implementation("net.perfectdreams.discordinteraktions:webserver-ktor-kord:${Versions.DISCORD_INTERAKTIONS}")
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")

    // Sequins
    api("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    // Logging
    api("ch.qos.logback:logback-classic:1.3.0-alpha14")

    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

jib {
    container {
        ports = listOf("8080")
        mainClass = "net.perfectdreams.loritta.cinnamon.platform.webserver.LorittaCinnamonWebServerLauncher"
    }

    to {
        image = "ghcr.io/lorittabot/cinnamon-web-server"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "openjdk:17-slim-bullseye"
    }
}

tasks {
    processResources {
        from("../../resources/") // Include folders from the resources root folder
    }
}