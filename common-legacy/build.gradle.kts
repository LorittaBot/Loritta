plugins {
    kotlin("multiplatform")
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
                api(project(":common")) // hehe

                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLIN_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                api("io.ktor:ktor-client-apache:${Versions.KTOR}")

                // Used for the LocaleManager
                implementation("org.yaml:snakeyaml:1.28")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api(npm("canvas", "2.6.1"))
                api("io.ktor:ktor-client-js:${Versions.KTOR}")
            }
        }
    }
}