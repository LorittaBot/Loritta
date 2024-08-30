plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.7.0-alpha03"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // Live Literals seems to be only used for hot reloading in dev mode, but Compose Web doesn't support hot reload yet
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:liveLiterals=false",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:liveLiteralsEnabled=false"
        )
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
                implementation(npm("marked", "9.1.0"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.599")
            }

            resources.srcDir("../../resources/") // Include folders from the resources web folder
        }
    }
}