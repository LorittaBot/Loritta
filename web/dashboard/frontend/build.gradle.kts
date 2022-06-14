plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.0.0-on_kotlin_1.7.0-rc-dev705"
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// Enable JS(IR) target and add dependencies
kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation(libs.ktor.client.core)
                implementation(project(":web:dashboard:common"))
                implementation(libs.ktor.client.js)
            }

            resources.srcDir("../../resources/") // Include folders from the resources web folder
        }
    }
}