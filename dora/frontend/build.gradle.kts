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
                implementation(project(":luna:bliss"))
                implementation(project(":luna:toast-common"))
                implementation(project(":luna:toast-manager-frontend"))
                implementation(project(":luna:modal-common"))
                implementation(project(":luna:modal-manager-frontend"))
                implementation(libs.kotlinWrappers.browser)
                implementation(libs.kotlinWrappers.js)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}