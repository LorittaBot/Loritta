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
                api(project(":common"))
            }
        }

        // jvmMain and jsMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jsoup:jsoup:1.13.1")
            }
        }
        js().compilations["main"].defaultSourceSet {}
    }
}