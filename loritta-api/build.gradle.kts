plugins {
    kotlin("multiplatform") apply true
    kotlin("plugin.serialization") version "1.3.70"
}

kotlin {
    jvm()
    js()

    presets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsTargetPreset>().forEach {
        targetFromPreset(it) {
            this.nodejs
            this.browser
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.github.microutils:kotlin-logging-common:1.7.8")
                implementation("io.ktor:ktor-client-core:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.github.microutils:kotlin-logging:1.7.8")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
                implementation("io.ktor:ktor-client-apache:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.github.microutils:kotlin-logging-js:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
                implementation(npm("canvas", "2.6.1"))
                implementation("io.ktor:ktor-client-js:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
            }
        }
    }
}