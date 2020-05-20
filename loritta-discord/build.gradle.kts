import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val loriVersion by lazy { ext["lori-version"] as String }
val kotlinVersion by lazy { ext["kotlin-version"] as String }
val ktorVersion by lazy { ext["ktor-version"] as String }
val jdaVersion by lazy { ext["jda-version"] as String }

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.3.70"
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
    compile(project(":loritta-api"))
    compile(project(":loritta-serializable-commons"))
    compile(project(":temmie-discord-auth"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.5")
    compile("com.google.guava:guava:28.1-jre")
	compile(kotlin("script-util"))
	compile(kotlin("compiler"))
	compile(kotlin("scripting-compiler"))
    compile("net.perfectdreams.commands:command-framework-core:0.0.8")
    compile("com.oracle.graaljs:graal-js:1.0.0-rc9")
    compile("com.oracle.tregex:tregex:1.0.0-rc9")
    compile("com.oracle.truffle:truffle-api:1.0.0-rc7")
    compile("org.graalvm:graal-sdk:1.0.0-rc7")
    compile("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
    compile("org.twitter4j:twitter4j-core:[4.0,)")
    compile("org.twitter4j:twitter4j-stream:[4.0,)")
    compile("com.github.ben-manes.caffeine:caffeine:2.8.2")
    compile("net.dv8tion:JDA:$jdaVersion")
    compile("org.postgresql:postgresql:42.2.12")
    compile("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.4")
    compile("org.xerial:sqlite-jdbc:3.31.1")
    compile("com.zaxxer:HikariCP:3.4.3")
    // compile("org.jetbrains.exposed:exposed-core:0.24.1")
    // compile("org.jetbrains.exposed:exposed-dao:0.24.1")
    // compile("org.jetbrains.exposed:exposed-jdbc:0.24.1")
    compile(files("../exposed/exposed-core-0.24.1.jar"))
    compile(files("../exposed/exposed-dao-0.24.1.jar"))
    compile(files("../exposed/exposed-jdbc-0.24.1.jar"))
    compile("com.github.MrPowerGamerBR:TemmieWebhook:59de40c3b6")
    compile("org.apache.commons:commons-lang3:3.9")
    compile("commons-codec:commons-codec:1.13")
    compile("commons-io:commons-io:2.6")
    compile("org.apache.commons:commons-text:1.8")
    compile("org.apache.commons:commons-collections4:4.4")
    compile("com.github.FredBoat:Lavalink-Client:4.0")
    compile("io.ktor:ktor-server-core:$ktorVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("com.google.code.gson:gson:2.8.6")
    compile("io.github.microutils:kotlin-logging:1.7.9")
    compile("io.pebbletemplates:pebble:3.1.3")
    compile("org.jsoup:jsoup:1.13.1")
    compile("com.github.kevinsawicki:http-request:6.0")
    compile("com.rometools:rome:1.12.2")
    compile("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    compile("com.fasterxml.jackson.core:jackson-annotations:2.11.0")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
    compile("com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.0")
    compile("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")
    compile("com.github.markozajc:akiwrapper:1.4.3.2")
    compile("org.json:json:20190722")
    compile("com.github.salomonbrys.kotson:kotson:2.5.0")
    compile("com.vladsch.flexmark:flexmark-all:0.50.44")
    compile("de.siegmar:logback-gelf:3.0.0")
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