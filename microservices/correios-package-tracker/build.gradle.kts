import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.google.cloud.tools.jib") version Versions.JIB
}

dependencies {
    implementation(project(":pudding:client"))
    implementation(project(":discord:discord-common"))
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")
    implementation("pw.forst", "exposed-upsert", "1.1.0")
    implementation("com.zaxxer:HikariCP:${Versions.HIKARICP}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLINX_SERIALIZATION}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")
    implementation("dev.kord:kord-rest:${Versions.KORD}")

    // Logback GELF, used for Graylog logging
    implementation("de.siegmar:logback-gelf:3.0.0")

    testImplementation("ch.qos.logback:logback-classic:1.3.0-alpha14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

tasks.withType<Test> {
    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    useJUnitPlatform()
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/corrieos-package-tracker"

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
        attributes["Main-Class"] = "net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.CorreiosPackageTrackerLauncher"
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