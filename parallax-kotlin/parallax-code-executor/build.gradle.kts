import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val loriVersion by lazy { ext["lori-version"] as String }
val kotlinVersion by lazy { ext["kotlin-version"] as String }
val ktorVersion by lazy { ext["ktor-version"] as String }

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.3.70"
    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    api(project(":loritta-api"))
    api(project(":parallax-kotlin:parallax-code-api"))
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "net.perfectdreams.loritta.parallax.executor.ParallaxCodeExecutor",
        mapOf()
)

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

tasks.test {
    useJUnitPlatform()
}