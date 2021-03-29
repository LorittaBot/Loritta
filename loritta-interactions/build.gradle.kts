plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.30"
}

repositories {
    jcenter()
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    api(project(":loritta-api"))
    api(kotlin("stdlib-jdk8"))

    // Logging
    api("org.slf4j:slf4j-api:2.0.0-alpha0")
    api("ch.qos.logback:logback-classic:1.3.0-alpha4")
    api("io.github.microutils:kotlin-logging:1.6.26")

    // Discord InteraKTions
    api("net.perfectdreams.discordinteraktions:core:0.0.3-SNAPSHOT")

    // Utils
    api("com.google.guava:guava:30.0-jre")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.1.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks {
    val fatJar = fatJarTask(
        configurations.runtimeClasspath.get(),
        jar.get(),
        "net.perfectdreams.loritta.interactions.LorittaInteractionsLauncher",
        mapOf()
    )

    "build" {
        dependsOn(fatJar)
    }
}

tasks.test {
    useJUnitPlatform()
}