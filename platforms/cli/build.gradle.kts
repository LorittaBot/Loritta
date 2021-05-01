import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version Versions.KOTLIN
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":commands"))
    implementation(project(":services:memory"))
    implementation("io.ktor:ktor-client-cio:1.5.3")
}

// CLI
tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.perfectdreams.loritta.platform.cli.LorittaCLILauncher"
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}