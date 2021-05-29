import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    java
    kotlin("jvm")
}

dependencies {
    api(project(":common"))
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    api("org.jetbrains.exposed:exposed-dao:${Versions.EXPOSED}")
    api("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
}