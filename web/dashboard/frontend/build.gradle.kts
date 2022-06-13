plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev709"
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
            }

            resources.srcDir("../../resources/") // Include folders from the resources web folder
        }
    }
}