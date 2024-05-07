plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "net.perfectdreams.showtime"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        withJava()
    }
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
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
                api(libs.kotlinx.serialization.json)
                api(libs.kotlin.logging)
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(npm("buffer", "5.6.1"))
            }
        }

        val jvmTest by getting {
            dependencies {
                // Required for tests, if this is missing then Gradle will throw
                // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.junit.jupiter:junit-jupiter:5.4.2")
            }
        }
    }
}