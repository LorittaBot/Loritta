import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":pudding:client"))
    implementation(project(":discord:discord-common"))
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
    implementation("pw.forst", "exposed-upsert", "1.1.0")
    implementation(libs.hikaricp)
    implementation("dev.kord:kord-rest:${Versions.KORD}")
    implementation("dev.kord:kord-gateway:${Versions.KORD}")
    implementation("dev.kord:kord-voice:0.8.x-20220720.101712-210")
    implementation("org.gagravarr:vorbis-java-core:0.8")
    implementation(libs.ktor.client.cio)
    implementation("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.3")
    implementation("commons-codec:commons-codec:1.15")

    // Logback GELF, used for Graylog logging
    implementation("de.siegmar:logback-gelf:3.0.0")

    testImplementation(libs.logback.classic)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

tasks.withType<Test> {
    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    useJUnitPlatform()
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/discord-gateway-events-processor"

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

tasks {
    processResources {
        from("../../resources/") // Include folders from the resources root folder
    }
}