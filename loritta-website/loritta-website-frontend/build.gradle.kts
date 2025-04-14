plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.github.turansky.kfc.latest-webpack")
}

group = "net.perfectdreams.showtime"
version = "1.0-SNAPSHOT"

kotlin {
    js(IR) { // Use new, but experimental, compiler
        browser {
            binaries.executable()
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":loritta-website:web-common"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
                implementation(libs.ktor.client.core)
            }
        }
    }
}