import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    maven("https://repo.perfectdreams.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
}

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
                implementation(project(":common"))
                implementation(project(":commands"))
                implementation(project(":services:memory"))

                implementation("blue.starry:penicillin:6.1.0")
                implementation("io.ktor:ktor-client-cio:1.5.4")
            }
        }
    }
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.perfectdreams.loritta.platform.twitter.LorittaTwitterLauncher"
    }
}