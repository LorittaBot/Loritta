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
    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    api(project(":loritta-api"))
    api(kotlin("stdlib-jdk8"))
    api("org.slf4j:slf4j-api:2.0.0-alpha0")
    api("ch.qos.logback:logback-classic:1.3.0-alpha4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    api("com.github.ben-manes.caffeine:caffeine:2.7.0")
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-websockets:$ktorVersion")
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio:$ktorVersion")
    api("io.github.microutils:kotlin-logging:1.6.26")
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
        "net.perfectdreams.loritta.shardcontroller.ShardControllerServerLauncher",
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