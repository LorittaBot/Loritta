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
        jsMain {
            dependencies {
                implementation(project(":luna:toast-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.12.0")
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinWrappers.browser)
            }
        }
    }
}