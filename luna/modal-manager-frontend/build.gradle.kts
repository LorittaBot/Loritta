import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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
                implementation(project(":luna:modal-common"))
                implementation("net.perfectdreams.compose.htmldreams:html-core:1.9.0-beta1-v2")
                api(compose.runtime)
                implementation(libs.kotlinWrappers.browser)
            }
        }
    }
}