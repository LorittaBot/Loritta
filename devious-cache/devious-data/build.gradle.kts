import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Versions.KOTLIN
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

dependencies {
    api(project(":common"))
    api(kotlin("stdlib-jdk8"))

    implementation("dev.kord:kord-common") {
        version {
            strictly("0.8.x-lori-fork-20221109.172532-16")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}