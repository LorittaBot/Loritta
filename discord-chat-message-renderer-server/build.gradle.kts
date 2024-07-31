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
    implementation(project(":discord-chat-message-renderer"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.1")
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // Logging Stuff
    implementation(libs.logback.classic)
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