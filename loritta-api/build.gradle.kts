plugins {
    kotlin("multiplatform") apply true
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
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.github.microutils:kotlin-logging-common:1.7.8")
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("io.github.microutils:kotlin-logging:1.7.8")
                api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                api("com.github.salomonbrys.kotson:kotson:2.5.0")
                api("io.ktor:ktor-client-apache:${Versions.KTOR}")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api("io.github.microutils:kotlin-logging-js:1.7.8")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
                api(npm("canvas", "2.6.1"))
                api("io.ktor:ktor-client-js:${Versions.KTOR}")
            }
        }
    }
}