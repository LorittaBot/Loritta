plugins {
    kotlin("multiplatform") apply true
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
        val commonMain by getting {
            dependencies {
                api(project(":loritta-api"))
                api("com.ionspin.kotlin:bignum:0.3.0-SNAPSHOT")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api(project(":loritta-api"))
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api(project(":loritta-api"))
                api("com.ionspin.kotlin:bignum:0.2.8")
            }
        }
    }
}