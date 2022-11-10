import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
}

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":loritta-serializable-commons"))
    implementation(project(":pudding:client"))
    implementation(project(":temmie-discord-auth"))
    implementation(project(":devious-cache:devious-data"))

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.KOTLIN_COROUTINES}")

    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("org.twitter4j:twitter4j-core:4.0.7")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")

    // Discord
    // implementation("net.dv8tion:JDA:${Versions.JDA}")
    implementation("club.minnced:discord-webhooks:0.5.7")

    // Discord InteraKTions my beloved
    // We only depend in the common module here, the interactions/gateway will have the proper Discord InteraKTions modules related to them.
    implementation("net.perfectdreams.discordinteraktions:common:${Versions.DISCORD_INTERAKTIONS}")
    implementation("net.perfectdreams.discordinteraktions:gateway-kord:${Versions.DISCORD_INTERAKTIONS}")

    // Used to serialize state on components
    implementation("io.github.netvl.ecoji:ecoji:1.0.0")

    // We want to use Kord on our project too!
    implementation("dev.kord:kord-common") {
        version {
            strictly("0.8.x-lori-fork-20221109.172532-16")
        }
    }
    implementation("dev.kord:kord-rest") {
        version {
            strictly("0.8.x-lori-fork-20221109.172532-14")
        }
    }
    implementation("dev.kord:kord-gateway") {
        version {
            strictly("0.8.x-lori-fork-20221109.172532-15")
        }
    }
    implementation("dev.kord:kord-core") {
        version {
            strictly("0.8.x-lori-fork-20221109.172532-14")
        }
    }
    implementation("dev.kord:kord-voice:0.8.x-lori-fork-20221109.172532-15")

    // Exposed & Databases
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.javatime)
    api(libs.exposed.dao)
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.1.0")
    implementation("pw.forst:exposed-upsert:1.1.0")

    // DreamStorageService
    implementation("net.perfectdreams.dreamstorageservice:client:2.0.2")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.commons:commons-collections4:4.4")

    // Ktor
    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-websockets:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-apache:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-okhttp:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-sessions:${Versions.KTOR}")

    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.pebbletemplates:pebble:3.1.4")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.github.kevinsawicki:http-request:6.0")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.3")
    implementation("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")

    implementation("org.json:json:20190722")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation("com.vladsch.flexmark:flexmark-all:0.62.2")

    // Graylog GELF (Logback)
    implementation("de.siegmar:logback-gelf:3.0.0")

    // Prometheus
    implementation("io.prometheus:simpleclient:0.9.0")
    implementation("io.prometheus:simpleclient_hotspot:0.9.0")
    implementation("io.prometheus:simpleclient_common:0.9.0")

    // Sequins
    implementation("net.perfectdreams.sequins.text:text-utils:1.0.1")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    implementation("net.perfectdreams.randomroleplaypictures:client:1.0.1")
    implementation("org.gagravarr:vorbis-java-core:0.8")

    // zstd
    implementation("com.github.luben:zstd-jni:1.5.2-4")

    // fastutil
    implementation("it.unimi.dsi:fastutil:8.5.9")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    processResources {
        from("../../resources/") // Include folders from the resources root folder
    }
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.morenitta.LorittaLauncher"
        environment = environment.toMutableMap().apply {
            this["COMMIT_SHA"] = System.getProperty("GITHUB_SHA", null)
        }
    }

    to {
        image = "ghcr.io/lorittabot/loritta-morenitta"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "tar://${File(rootDir, "docker/image.tar").absoluteFile}"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}