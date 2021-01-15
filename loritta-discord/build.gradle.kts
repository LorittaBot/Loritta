import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val loriVersion by lazy { ext["lori-version"] as String }
val kotlinVersion by lazy { ext["kotlin-version"] as String }
val ktorVersion by lazy { ext["ktor-version"] as String }
val jdaVersion by lazy { ext["jda-version"] as String }
val kotlinSerialization by lazy { ext["kotlin-serialization"] as String }
val kotlinCoroutines by lazy { ext["kotlin-coroutines"] as String }

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.10"
    `maven-publish`
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":loritta-api"))
    api(project(":loritta-serializable-commons"))
    api(project(":temmie-discord-auth"))

    // Kotlin Scripting
    api(kotlin("script-util"))
    api(kotlin("compiler"))
    api(kotlin("scripting-compiler"))

    // Kotlin Serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialization")

    // Kotlin Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$kotlinCoroutines")

    api("com.google.guava:guava:30.0-jre")

    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
    api("org.twitter4j:twitter4j-core:[4.0,)")
    api("com.github.ben-manes.caffeine:caffeine:2.8.6")

    // Discord
    api("net.dv8tion:JDA:$jdaVersion")
    api("club.minnced:discord-webhooks:0.5.0")

    // Exposed & Databases
    api("org.postgresql:postgresql:42.2.18")
    api("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.6")
    api("org.xerial:sqlite-jdbc:3.32.3.2")
    api("com.zaxxer:HikariCP:3.4.5")
    api("org.jetbrains.exposed:exposed-core:0.28.1")
    api("org.jetbrains.exposed:exposed-dao:0.28.1")
    api("org.jetbrains.exposed:exposed-jdbc:0.28.1")

    api("org.apache.commons:commons-lang3:3.11")
    api("commons-codec:commons-codec:1.15")
    api("commons-io:commons-io:2.8.0")
    api("org.apache.commons:commons-text:1.9")
    api("org.apache.commons:commons-collections4:4.4")

    // Ktor
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-apache:$ktorVersion")

    api("com.google.code.gson:gson:2.8.6")
    api("io.github.microutils:kotlin-logging:2.0.3")
    api("io.pebbletemplates:pebble:3.1.4")
    api("org.jsoup:jsoup:1.13.1")
    api("com.github.kevinsawicki:http-request:6.0")
    api("com.rometools:rome:1.15.0")

    // Jackson
    api("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    api("com.fasterxml.jackson.core:jackson-annotations:2.11.3")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.3")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.3")
    api("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")

    api("org.json:json:20190722")
    api("com.github.salomonbrys.kotson:kotson:2.5.0")
    api("com.vladsch.flexmark:flexmark-all:0.62.2")

    // Graylog GELF (Logback)
    api("de.siegmar:logback-gelf:3.0.0")

    // Prometheus
    api("io.prometheus:simpleclient:0.9.0")
    api("io.prometheus:simpleclient_hotspot:0.9.0")
    api("io.prometheus:simpleclient_common:0.9.0")

    // Sequins
    api("net.perfectdreams.sequins.text:text-utils:1.0.0")
    api("net.perfectdreams.sequins.ktor:base-route:1.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}

tasks.test {
    useJUnitPlatform()
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "com.mrpowergamerbr.loritta.LorittaLauncher",
        mapOf(
                "JDA-Version" to jdaVersion
        )
)

tasks {
    "build" {
        dependsOn(fatJar)
    }
}