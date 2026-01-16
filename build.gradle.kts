import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("org.jetbrains.compose") version libs.versions.compose
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin apply false
    id("org.jetbrains.kotlin.plugin.js-plain-objects") version libs.versions.kotlin apply false
}

allprojects {
    group = "net.perfectdreams.loritta"
    version = Versions.LORITTA

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")

        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        maven("https://repo.perfectdreams.net/")
        maven("https://jitpack.io")

        // Used by JDA
        maven("https://m2.dv8tion.net/releases")
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(Versions.JVM_TARGET.toInt())
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(Versions.JVM_TARGET.toInt())
        }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            this.javaParameters = true
        }
    }
}

// Gradle Build Scan
// https://stackoverflow.com/a/56634703/7271796
extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}