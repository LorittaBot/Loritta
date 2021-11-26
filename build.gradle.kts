plugins {
    // It needs to be in here to avoid a "Failed to apply plugin class 'org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin'."
    kotlin("multiplatform") version Versions.KOTLIN apply false
    kotlin("jvm") version Versions.KOTLIN apply false
    kotlin("plugin.serialization") version Versions.KOTLIN apply false
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
        mavenCentral()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
    }
}

// Workaround for https://kotlinlang.slack.com/archives/C0B8L3U69/p1633590092096600
// Remove this when Loritta updates to Kotlin 1.6.0+
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        resolution("@webpack-cli/serve", "1.5.2")
    }
}