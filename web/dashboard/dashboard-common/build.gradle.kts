plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) { // Use new, but experimental, compiler
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.kotlin.logging)
                api(project(":common"))
                api(project(":loritta-serializable-commons"))
            }
        }
    }
}