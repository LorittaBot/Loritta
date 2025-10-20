plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.4.5"
}

dependencies {
    implementation("io.ktor:ktor-server-cio:3.2.3")
    implementation("io.ktor:ktor-client-cio:3.2.3")

    // Logging
    implementation("net.perfectdreams.harmony.logging:harmonylogging-slf4j:1.0.2")
    implementation("ch.qos.logback:logback-classic:1.5.19")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.9.0")
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.dashboard.backend.LorittaDashboardBackendLauncher"
    }

    to {
        image = "ghcr.io/lorittabot/loritta-dashboard-backend-proxy"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "eclipse-temurin:24-noble"
    }
}