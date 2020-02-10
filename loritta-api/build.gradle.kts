plugins {
    kotlin("multiplatform") apply true
}

kotlin {
    jvm()
    js()

    presets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsTargetPreset>().forEach {
        targetFromPreset(it) {
            this.nodejs
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.github.microutils:kotlin-logging-common:1.7.8")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.github.microutils:kotlin-logging:1.7.8")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.github.microutils:kotlin-logging-js:1.7.8")
            }
        }
    }
}