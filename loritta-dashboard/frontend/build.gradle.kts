plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.9.0-beta01"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.js-plain-objects") version "2.2.0"
}

val commitHash = System.getProperty("commit.hash")
val buildId = System.getProperty("build.id")

val generateBuildInfo by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildinfo/kotlin")
    outputs.dir(outputDir)

    doLast {
        val file = outputDir.get().file("FrontendBuildInfo.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package net.perfectdreams.loritta.dashboard.frontend

            object FrontendBuildInfo {
                val COMMIT_HASH: String? = ${if (commitHash != null) "\"$commitHash\"" else "null"}
                val BUILD_ID: Int? = ${buildId ?: "null"}
            }
            """.trimIndent()
        )
    }
}

repositories {
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
        }
    }

    sourceSets {
        jsMain {
            kotlin.srcDir(generateBuildInfo)

            dependencies {
                implementation("net.perfectdreams.compose.htmldreams:html-core:1.9.0-beta01")
                implementation(compose.runtime)

                implementation("io.ktor:ktor-client-js:3.2.3")
                implementation("io.ktor:ktor-client-websockets:3.2.3")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js:2025.8.1")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2025.8.1")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")

                implementation("net.perfectdreams.harmony.logging:harmonylogging-common:1.0.2")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>().configureEach {
    dependsOn(generateBuildInfo)
}