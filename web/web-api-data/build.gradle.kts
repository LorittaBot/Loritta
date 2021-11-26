plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                // API = We want to allow dependencies to access those classes
                api(kotlin("stdlib-common"))
                
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
            }
        }
    }
}