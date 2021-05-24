plugins {
    kotlin("multiplatform")
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
                api("io.github.microutils:kotlin-logging:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLIN_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")

                // Used for Math stuff
                api("com.ionspin.kotlin:bignum:0.3.0")
            }
        }

        // jvmMain and jsMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-jdk8"))

                // Async Appender is broken in alpha5
                // https://stackoverflow.com/questions/58742485/logback-error-no-attached-appenders-found
                api("ch.qos.logback:logback-classic:1.3.0-alpha4")

                // Used for caching
                api("com.github.ben-manes.caffeine:caffeine:3.0.1")

                // Used for config
                api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:${Versions.KOTLIN_SERIALIZATION}")

                // Used for the LocaleManager
                implementation("org.yaml:snakeyaml:1.28")

                // Used by Minecraft related commands
                api("net.perfectdreams.minecraftmojangapi:minecraft-mojang-api:0.0.1-SNAPSHOT")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                // Required for tests, if this is missing then Gradle will throw
                // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.junit.jupiter:junit-jupiter:5.4.2")
                implementation("org.assertj:assertj-core:3.19.0")
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-js"))
            }
        }
    }
}