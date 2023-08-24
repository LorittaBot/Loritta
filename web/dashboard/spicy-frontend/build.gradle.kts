plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.4.3"
    id("io.github.turansky.kfc.latest-webpack")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile> {
    kotlinOptions {
        // Jetpack Compose doesn't support Kotlin 1.7.10 yet, but the latest version seems to compile just fine under Kotlin 1.7.10
        freeCompilerArgs += listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true")
    }
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
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(libs.ktor.client.core)
                implementation(project(":web:dashboard:dashboard-common"))
                implementation(libs.ktor.client.js)
                implementation(npm("pixi.js", "7.2.4"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.599")
            }

            resources.srcDir("../../resources/") // Include folders from the resources web folder
        }
    }
}