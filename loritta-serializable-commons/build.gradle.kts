plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenLocal()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = Versions.JVM_TARGET
        }
        withJava()
    }

    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api(project(":common"))
                api(project(":pudding:data"))
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        // Default source set for JS-specific sources and dependencies:
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}