import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(project(":common"))
    implementation(project(":discord:discord-common"))
    implementation(project(":discord:commands"))

    // Discord InteraKTions (Web Server)
    implementation("net.perfectdreams.discordinteraktions:gateway-kord:${Versions.DISCORD_INTERAKTIONS}")
    implementation("dev.kord:kord-gateway:${Versions.KORD}")

    // Logging
    api("ch.qos.logback:logback-classic:1.3.0-alpha14")

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
        mainClass = "net.perfectdreams.loritta.cinnamon.platform.gateway.LorittaCinnamonGatewayLauncher"
    }

    to {
        image = "ghcr.io/lorittabot/cinnamon-gateway"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "openjdk:17-slim-bullseye"
    }
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.perfectdreams.loritta.cinnamon.platform.gateway.LorittaCinnamonGatewayLauncher"
    }
}

tasks {
    processResources {
        from("../../resources/") // Include folders from the resources root folder
    }

    build {
        dependsOn(shadowJar)
    }
}