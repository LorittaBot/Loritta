plugins {
    kotlin("multiplatform") version Versions.KOTLIN
}

repositories {
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
                implementation("dev.kord:kord-core:0.7.x-SNAPSHOT")
            }
        }
    }
}