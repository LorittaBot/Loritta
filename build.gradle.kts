import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version Versions.KOTLIN apply false
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("io.github.turansky.kfc.latest-webpack") version "7.21.0" apply false
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
    tasks.withType<KotlinCompile> {
        kotlinOptions.javaParameters = true
    }
}

// Gradle Build Scan
// https://stackoverflow.com/a/56634703/7271796
extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

Loritta é feito com [ Kotlin ] ( https://kotlinlang.org/ ) . Kotlin é uma linguagem de programação moderna, concisa e segura feita pela JetBrains. Se você já usou Java antes, Kotlin parecerá familiar para você. Caramba, ele pode até rodar na JVM!
