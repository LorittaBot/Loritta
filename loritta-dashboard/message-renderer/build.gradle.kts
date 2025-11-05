import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()

        compilerOptions {
            // Enable ES6
            target = "es2015"
            useEsClasses = true
            moduleKind.set(JsModuleKind.MODULE_ES)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":loritta-placeholders"))
                implementation(project(":loritta-dashboard:dashboard-common"))
                implementation(project(":discord-chat-markdown-parser"))
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
                implementation(libs.kotlinx.serialization.core)
            }
        }
    }
}