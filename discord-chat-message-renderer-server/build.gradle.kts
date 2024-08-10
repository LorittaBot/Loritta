plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":discord-chat-markdown-parser"))
    implementation(project(":discord-chat-message-renderer-entities"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.1")
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.10.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.KOTLIN_COROUTINES}")

    // Used for logs - MojangStyleFileAppenderAndRollover
    implementation("com.github.luben:zstd-jni:1.5.5-6")

    // Playwright
    implementation("com.microsoft.playwright:playwright:1.45.0")

    // Discord
    implementation("com.github.LorittaBot:DeviousJDA:40ea50aea7")

    // Logging Stuff
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
}

val sassMessageRenderer = tasks.register<SassTask>("sass-message-renderer") {
    this.inputSass.set(file("src/main/sass-message-renderer/style.scss"))
    this.inputSassFolder.set(file("src/main/sass-message-renderer/"))
    this.outputSass.set(file("$buildDir/sass/sass-message-renderer-scss"))
}

tasks {
    processResources {
        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(sassMessageRenderer)

        // Same thing with the SASS output
        from(sassMessageRenderer) {
            into("message-renderer-assets/")
        }
    }
}

jib {
    container {
        environment = environment.toMutableMap().apply {
            this["PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD"] = "true"
        }
    }

    to {
        image = "ghcr.io/lorittabot/discord-chat-message-renderer-server"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "tar://${File(rootDir, "docker/image.tar").absoluteFile}"
    }
}