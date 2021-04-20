plugins {
    kotlin("multiplatform")
}

repositories {
    maven("https://repo.perfectdreams.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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
                implementation(project(":discord"))
                implementation("net.perfectdreams.discordinteraktions:core:0.0.4-SNAPSHOT")
            }
        }
    }
}