plugins {
    kotlin("multiplatform")
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
                implementation(project(":discord:common"))
                implementation("dev.kord:kord-core:0.8.x-SNAPSHOT")
            }
        }
    }
}