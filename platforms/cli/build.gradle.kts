import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":commands"))
    implementation(project(":services:memory"))
    implementation("io.ktor:ktor-client-cio:1.5.3")

    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("org.assertj:assertj-core:3.19.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

// CLI
tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.perfectdreams.loritta.platform.cli.LorittaCLILauncher"
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}