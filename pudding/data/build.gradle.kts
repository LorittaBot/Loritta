plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
}

version = Versions.PUDDING

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    js(BOTH) { // We compile for both because Loritta Legacy also uses this module
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
                
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLINX_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATE_TIME}")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")

            credentials {
                username = System.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = System.getProperty("PASSWORD") ?: System.getenv("PASSWORD")
            }
        }
    }
}