plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    js {
        // Declares that we want to compile for the browser and for nodejs
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                // API = We want to allow dependencies to access those classes
                api(kotlin("stdlib-common"))
                api("io.github.microutils:kotlin-logging:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
            }
        }

        // jvmMain and jsMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-jdk8"))

                // Async Appender is broken in alpha5
                // https://stackoverflow.com/questions/58742485/logback-error-no-attached-appenders-found
                api("ch.qos.logback:logback-classic:1.3.0-alpha4")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                // Required for tests, if this is missing then Gradle will throw
                // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.junit.jupiter:junit-jupiter:5.4.2")
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-js"))
            }
        }
    }
}