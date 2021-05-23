import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    java
    kotlin("jvm")
}

dependencies {
    api(project(":common-legacy"))
    api(kotlin("stdlib-jdk8"))
    api("org.slf4j:slf4j-api:2.0.0-alpha0")
    api("ch.qos.logback:logback-classic:1.3.0-alpha4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    api("com.github.ben-manes.caffeine:caffeine:2.7.0")
    api("io.ktor:ktor-server-core:${Versions.KTOR}")
    api("io.ktor:ktor-server-netty:${Versions.KTOR}")
    api("io.ktor:ktor-websockets:${Versions.KTOR}")
    api("io.ktor:ktor-client-core:${Versions.KTOR}")
    api("io.ktor:ktor-client-cio:${Versions.KTOR}")
    api("io.github.microutils:kotlin-logging:1.6.26")
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

tasks.test {
    useJUnitPlatform()
}