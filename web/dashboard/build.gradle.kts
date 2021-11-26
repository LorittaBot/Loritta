// Add compose gradle plugin
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-alpha4-build362"
}

// Add maven repositories
repositories {
    mavenCentral()
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
                implementation(project(":web:web-api-data"))
                implementation(npm("lottie-web", "5.8.1"))
                implementation("io.ktor:ktor-client-js:1.6.5")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("com.ionspin.kotlin:bignum:0.3.3")
            }
        }
    }
}