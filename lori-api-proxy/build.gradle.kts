plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":loritta-serializable-commons"))
    implementation(project(":lori-public-http-api-common"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.metricsMicrometer)

    implementation("io.micrometer:micrometer-registry-prometheus:1.13.6")

    // Used for logs - MojangStyleFileAppenderAndRollover
    implementation("com.github.luben:zstd-jni:1.5.5-6")
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.apiproxy.LoriAPIProxyLauncher"
    }

    to {
        image = "ghcr.io/lorittabot/lori-api-proxy"

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