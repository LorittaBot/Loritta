val ktorVersion by lazy { ext["ktor-version"] as String }

plugins {
    kotlin("multiplatform") apply true
    kotlin("plugin.serialization") version "1.4.10"
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
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("io.github.microutils:kotlin-logging:1.7.8")
                api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                api("com.github.salomonbrys.kotson:kotson:2.5.0")
                api("io.ktor:ktor-client-apache:$ktorVersion")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api("io.github.microutils:kotlin-logging-js:1.7.8")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
                api(npm("canvas", "2.6.1"))
                api("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
    }
    /* jvm()
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
                implementation("io.ktor:ktor-client-core:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.github.microutils:kotlin-logging:1.7.8")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
                implementation("io.ktor:ktor-client-apache:1.4.1")
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.github.microutils:kotlin-logging-js:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
                implementation(npm("canvas", "2.6.1"))
                implementation("io.ktor:ktor-client-js:1.4.1")
            }
        }
    } */
}