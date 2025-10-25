import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    js {
        browser()
        binaries.executable()

        compilerOptions {
            // Enable ES6
            target = "es2015"
            useEsClasses = true
            moduleKind.set(JsModuleKind.MODULE_ES)
        }
    }

    sourceSets {
        jsMain {
            dependencies {
                api(project(":loritta-dashboard:bliss-common"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2025.9.4")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js:2025.9.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            }
        }
    }
}