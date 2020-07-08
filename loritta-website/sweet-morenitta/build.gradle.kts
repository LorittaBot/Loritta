plugins {
    kotlin("multiplatform") apply true
    kotlin("plugin.serialization") version "1.3.70"
}

kotlin {
    jvm()
    js()

    presets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsTargetPreset>().forEach {
        targetFromPreset(it) {
            this.browser
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":loritta-api"))
                api("org.jetbrains.kotlinx:kotlinx-html-common:0.6.11")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":loritta-api"))
                api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.11")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(project(":loritta-api"))
                api("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")
            }
        }
    }
}