import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.9.0-beta01"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.js-plain-objects") version "2.2.0"
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
                implementation(project(":loritta-dashboard:dashboard-common"))
                implementation(project(":loritta-dashboard:message-renderer"))

                implementation("net.perfectdreams.compose.htmldreams:html-core:1.9.0-beta01")
                implementation(compose.runtime)

                implementation("io.ktor:ktor-client-js:3.2.3")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2025.9.4")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js:2025.9.4")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.12.0")
            }
        }
    }
}