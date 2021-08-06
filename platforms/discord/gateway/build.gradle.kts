import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":commands"))
    implementation(project(":services:memory"))
    implementation(project(":services:pudding"))
    implementation(project(":platforms:discord:common"))
    implementation(project(":platforms:discord:commands"))
    implementation("dev.kord:kord-core:0.7.x-SNAPSHOT")

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