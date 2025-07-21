plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
    id("net.perfectdreams.i18nhelper.plugin") version libs.versions.i18nhelperplugin
}

val generateI18nKeys = tasks.register<net.perfectdreams.i18nhelper.plugin.GenerateI18nKeysTask>("generateI18nKeys") {
    generatedPackage.set("net.perfectdreams.loritta.helper.i18n")
    languageSourceFolder.set(file("src/main/resources/languages/en/"))
    languageTargetFolder.set(file("$buildDir/generated/languages"))
    translationLoadTransform.set { file, map -> map }
}

group = "net.perfectdreams.loritta.helper"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.perfectdreams.net/")
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":loritta-serializable-commons"))
    implementation(project(":pudding:client"))
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.microutils:kotlin-logging:2.1.21")

    implementation(libs.deviousjda)
    implementation("com.github.freya022:jda-ktx:8929de93af")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.2")

    // Sequins
    implementation("net.perfectdreams.sequins.text:text-utils:1.0.0")

    // Used to serialize state on components
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.2")
    // Used to serialize state on components
    implementation("io.github.netvl.ecoji:ecoji:1.0.0")

    // i18nHelper
    api("net.perfectdreams.i18nhelper.formatters:icu-messageformat-jvm:0.0.5-SNAPSHOT")

    // GalleryOfDreams client
    implementation("net.perfectdreams.galleryofdreams:common:1.0.13")
    implementation("net.perfectdreams.galleryofdreams:client:1.0.13")

    // Used for the LocaleManager
    implementation("org.yaml:snakeyaml:2.4")
    implementation("com.charleskorn.kaml:kaml:0.80.1")

    // ICU
    implementation("com.ibm.icu:icu4j:71.1")

    // Database
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.60.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.60.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.60.0")
    implementation("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.2.1")
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.2.1")
    api("net.perfectdreams.exposedpowerutils:exposed-power-utils:1.2.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    implementation("io.ktor:ktor-client-cio:3.1.3")

    implementation("org.apache.commons:commons-text:1.9")

    implementation("com.github.luben:zstd-jni:1.5.5-6")
}

jib {
    to {
        image = "ghcr.io/lorittabot/loritta-helper"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:24-noble"
    }
}

sourceSets.main {
    java.srcDir(generateI18nKeys)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}