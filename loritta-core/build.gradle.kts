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
    compile(project(":loritta-api-jvm"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.0-M1")
    compile("com.google.guava:guava:28.0-jre")
	compile(kotlin("stdlib-jdk8"))
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
    compile("com.github.ben-manes.caffeine:caffeine:2.7.0")
    compile("javax.xml.bind:jaxb-api:2.3.1")
    compile("com.sun.xml.bind:jaxb-core:2.3.0.1")
    compile("com.sun.xml.bind:jaxb-impl:2.3.2")
    compile("javax.activation:activation:1.1.1")
    compile("net.dv8tion:JDA:$jdaVersion")
    compile("org.mongodb:mongodb-driver:3.10.2")
    compile("org.postgresql:postgresql:42.2.5")
    compile("com.zaxxer:HikariCP:3.3.1")
    compile("org.jetbrains.exposed:exposed:0.14.4")
    compile("com.github.MrPowerGamerBR:TemmieWebhook:59de40c3b6")
    compile("org.apache.commons:commons-lang3:3.9")
    compile("commons-codec:commons-codec:1.12")
    compile("commons-io:commons-io:2.6")
    compile("org.apache.commons:commons-text:1.6")
    compile("org.apache.commons:commons-collections4:4.4")
    compile("org.jooby:jooby:1.6.2")
    compile("org.jooby:jooby-netty:1.6.2")
    compile("org.jooby:jooby-lang-kotlin:1.6.2")
    compile("org.jooby:jooby-mongodb:1.6.2")
    compile("io.ktor:ktor-server-core:$ktorVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("io.ktor:ktor-client-cio:$ktorVersion")
    compile("io.github.microutils:kotlin-logging:1.6.26")
    compile("io.pebbletemplates:pebble:3.0.10")
    compile("org.jsoup:jsoup:1.12.1")
    compile("com.github.kevinsawicki:http-request:6.0")
    compile("com.rometools:rome:1.12.0")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.9")
    compile("com.fasterxml.jackson.core:jackson-annotations:2.9.9")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    compile("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.9")
    compile("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")
    compile("com.sedmelluq:lavaplayer:1.3.19")
    compile("com.github.FredBoat:Lavalink-Client:jda4-SNAPSHOT")
    compile("com.github.markozajc:akiwrapper:1.4.3.1")
    compile("org.json:json:20180813")
    compile("com.github.salomonbrys.kotson:kotson:2.5.0")
    compile("com.vladsch.flexmark:flexmark-all:0.50.26")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}


tasks.test {
    useJUnitPlatform()
}