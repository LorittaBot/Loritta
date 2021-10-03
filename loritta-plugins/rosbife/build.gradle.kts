plugins {
    kotlin("multiplatform") apply true
}

kotlin {
    jvm()

    sourceSets {
        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":platforms:discord:legacy"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
            }
        }
    }
}