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
    implementation(project(":temmie-discord-auth-loritta-commons"))
    implementation(project(":switch-twitch"))
    implementation(project(":discord-chat-message-renderer-entities"))

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.KOTLIN_COROUTINES}")

    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.10.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Discord
    implementation("com.github.LorittaBot:DeviousJDA:40ea50aea7")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")

    // Used to serialize state on components
    implementation("io.github.netvl.ecoji:ecoji:1.0.0")

    // We want to use Kord on our project too!
    implementation("dev.kord:kord-rest:0.8.x-lori-fork-20221109.172532-14")
    implementation("dev.kord:kord-gateway:0.8.x-lori-fork-20221109.172532-15")
    implementation("dev.kord:kord-core:0.8.x-lori-fork-20221109.172532-14")
    implementation("dev.kord:kord-voice:0.8.x-lori-fork-20221109.172532-15")

    // Exposed & Databases
    implementation(libs.postgresqljdbcdriver)
    implementation(libs.hikaricp)
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.javatime)
    api(libs.exposed.dao)
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.2.1")

    // DreamStorageService
    implementation("net.perfectdreams.dreamstorageservice:client:2.0.2")

    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.apache.commons:commons-collections4:4.4")

    // Ktor
    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-websockets:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-apache:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-sessions:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.pebbletemplates:pebble:3.1.4")
    implementation("org.jsoup:jsoup:1.17.1")
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

    // Sequins
    implementation("net.perfectdreams.sequins.text:text-utils:1.0.1")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    implementation("net.perfectdreams.randomroleplaypictures:client:1.0.1")
    implementation("org.gagravarr:vorbis-java-core:0.8")

    // GalleryOfDreams client
    implementation("net.perfectdreams.galleryofdreams:common:1.0.11")
    implementation("net.perfectdreams.galleryofdreams:client:1.0.11")

    // Used for logs - MojangStyleFileAppenderAndRollover
    implementation("com.github.luben:zstd-jni:1.5.5-6")

    // Used to render messages
    implementation("com.microsoft.playwright:playwright:1.45.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.12.2")
}

// This is required to workaround "org.gradle.api.internal.initialization.DefaultClassLoaderScope@29009529 must be locked before it can be used to compute a classpath!" issue
// No, I don't know why this happens, this only happens if the module is...
// :project (on the root directory)
// :project:subprojectjs (on the child directory)
evaluationDependsOn(":web:spicy-morenitta")

val jsBrowserDistribution = tasks.getByPath(":web:spicy-morenitta:jsBrowserDistribution")
val jsBrowserProductionWebpack = tasks.getByPath(":web:spicy-morenitta:jsBrowserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val sass = tasks.register<SassTask>("sass-style-scss") {
    this.inputSass.set(file("src/main/sass/style.scss"))
    this.inputSassFolder.set(file("src/main/sass/"))
    this.outputSass.set(file("$buildDir/sass/style-scss"))
}

val sassLegacy = tasks.register<SassTask>("sass-legacy-style-scss") {
    this.inputSass.set(file("src/main/sass-legacy/style.scss"))
    this.inputSassFolder.set(file("src/main/sass-legacy/"))
    this.outputSass.set(file("$buildDir/sass/style-legacy-scss"))
}

val sassDashboard = tasks.register<SassTask>("sass-dashboard-style-scss") {
    this.inputSass.set(file("src/main/sass-dashboard/style.scss"))
    this.inputSassFolder.set(file("src/main/sass-dashboard/"))
    this.outputSass.set(file("$buildDir/sass/style-dashboard-scss"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    processResources {
        from("../resources/") // Include folders from the resources root folder

        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(jsBrowserDistribution)
        dependsOn(jsBrowserProductionWebpack)
        dependsOn(sass)
        dependsOn(sassLegacy)

        // Copy the output from the frontend task to the backend resources
        from(jsBrowserProductionWebpack.destinationDirectory) {
            into("spicy_morenitta/js/")
        }

        // Same thing with the SASS output
        from(sass) {
            into("static/v2/assets/css/")
        }
        from(sassDashboard) {
            into("static/lori-slippy/assets/css/")
        }
        from(sassLegacy) {
            into("static/assets/css/")
        }
    }
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.morenitta.LorittaLauncher"
        environment = environment.toMutableMap().apply {
            fun setIfPresent(propName: String, envName: String) {
                val propValue = System.getProperty(propName, null)
                // Only set if it is not null, because if it is, Jib complains
                if (propValue != null)
                    this[envName] = propValue
            }

            setIfPresent("commit.hash", "COMMIT_HASH")
            setIfPresent("build.id", "BUILD_ID")
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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}