import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

dependencies {
    api(project(":common"))
    api(kotlin("stdlib-jdk8"))
    api("org.slf4j:slf4j-api:2.0.0-alpha0")
    api(libs.logback.classic)
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    api("com.github.ben-manes.caffeine:caffeine:2.7.0")
    api("io.ktor:ktor-server-core:${Versions.KTOR}")
    api("io.ktor:ktor-server-netty:${Versions.KTOR}")
    api("io.ktor:ktor-websockets:${Versions.KTOR}")
    api("io.ktor:ktor-client-core:${Versions.KTOR}")
    api("io.ktor:ktor-client-cio:${Versions.KTOR}")
    api("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING}")
    api("com.github.salomonbrys.kotson:kotson:2.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}

tasks {
    val fatJar = runnableJarTask(
        DEFAULT_SHADED_WITHIN_JAR_LIBRARIES,
        configurations.runtimeClasspath.get(),
        jar.get(),
        "net.perfectdreams.loritta.shardcontroller.ShardControllerServerLauncher",
        mapOf()
    )

    "build" {
        dependsOn(fatJar)
    }
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.shardcontroller"
    }

    to {
        image = "ghcr.io/lorittabot/shard-controller"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "eclipse-temurin:18-focal"
    }
}

tasks.test {
    useJUnitPlatform()
}