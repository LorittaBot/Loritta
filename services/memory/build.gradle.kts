plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    js {
        // Declares that we want to compile for the browser and for nodejs
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                // API = We want to allow dependencies to access those classes
                api(kotlin("stdlib-common"))
                api(project(":common"))
            }
        }
    }
}