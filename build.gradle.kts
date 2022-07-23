plugins {
    // It needs to be in here to avoid a "Failed to apply plugin class 'org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin'."
    kotlin("multiplatform") version libs.versions.kotlin apply false
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    id("io.github.turansky.kfc.latest-webpack") version "5.50.0" apply false
}

allprojects {
    // Example:
    // ":discord:interactions" will be transformed into "discord"
    // We need to do this because we have the ":discord:common" module and, if you don't change the
    // group coordinates, Gradle will detect it as a circular reference (but it isn't!)
    val splittedPath = this.path.split(":")
    group = if (splittedPath.size >= 3) {
        // No need for the dot because the first entry in the "splittedPath" is a empty string
        "net.perfectdreams.loritta.cinnamon${splittedPath.dropLast(1).joinToString(".")}"
    } else {
        "net.perfectdreams.loritta.cinnamon"
    }

    version = Versions.LORITTA

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
    }
}

// Gradle Build Scan
// https://stackoverflow.com/a/56634703/7271796
extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}