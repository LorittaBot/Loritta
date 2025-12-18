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
    jvm()

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
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.kotlinWrappers.browser)
                implementation(libs.kotlinWrappers.js)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}