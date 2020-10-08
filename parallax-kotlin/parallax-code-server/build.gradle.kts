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
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    api(project(":parallax-kotlin:parallax-code-api"))
    api(kotlin("stdlib-jdk8"))
    api(kotlin("script-util"))
    api(kotlin("compiler-embeddable"))
    api(kotlin("scripting-compiler-embeddable"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
    api("com.github.ben-manes.caffeine:caffeine:2.7.0")
    api("org.postgresql:postgresql:42.2.5")
    api("com.zaxxer:HikariCP:3.3.1")
    api("org.jetbrains.exposed:exposed:0.13.6")
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-websockets:$ktorVersion")
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio:$ktorVersion")
    api("org.jooby:jooby-mongodb:1.6.0")
    api("io.github.microutils:kotlin-logging:1.6.26")
    api("org.graalvm.js:js:20.0.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    api("com.fasterxml.jackson.core:jackson-annotations:2.9.8")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.8")
    api("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")
    api("com.github.salomonbrys.kotson:kotson:2.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "net.perfectdreams.loritta.parallax.ParallaxServerLauncher",
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