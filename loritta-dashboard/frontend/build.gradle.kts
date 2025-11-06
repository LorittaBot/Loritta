import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.js-plain-objects")
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
                implementation(project(":loritta-dashboard:bliss"))
                implementation(project(":loritta-dashboard:dashboard-common"))
                implementation(project(":loritta-placeholders"))
                implementation(project(":loritta-dashboard:message-renderer"))
                implementation(project(":loritta-dashboard:loritta-shimeji-common"))

                implementation("net.perfectdreams.compose.htmldreams:html-core:1.9.0-beta01")
                implementation(compose.runtime)

                implementation(libs.kotlinWrappers.browser)
                implementation(libs.kotlinWrappers.js)

                implementation(libs.kotlinx.serialization.json)

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.12.0")

                implementation(devNpm("webpack-bundle-analyzer", "4.10.2"))
            }
        }
    }
}