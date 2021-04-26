plugins {
    kotlin("multiplatform") version Versions.KOTLIN
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
                implementation(project(":in-memory-services"))
                implementation("io.ktor:ktor-client-cio:1.5.3")
            }
        }
    }
}