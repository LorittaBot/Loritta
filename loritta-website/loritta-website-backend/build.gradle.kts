plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

group = "net.perfectdreams.loritta.website"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":loritta-serializable-commons"))
    implementation(project(":loritta-website:web-common"))
    implementation(project(":pudding:client"))
    implementation(project(":temmie-discord-auth"))
    implementation(project(":temmie-discord-auth-loritta-commons"))

    // Logging Stuff
    implementation(libs.logback.classic)

    // Ktor
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-html-builder:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.KTOR}")
    implementation(libs.ktor.server.sessions)

    // KotlinX HTML
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")

    implementation("org.jsoup:jsoup:1.19.1")

    // YAML
    implementation("org.yaml:snakeyaml:2.4")
    implementation("com.charleskorn.kaml:kaml:0.80.1")

    implementation("net.perfectdreams.etherealgambi:client:1.0.0")

    api("commons-codec:commons-codec:1.15")

    api("com.vladsch.flexmark:flexmark-all:0.64.0")
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/loritta-website-backend"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:24-noble"
    }
}

val jsBrowserDistribution = tasks.getByPath(":loritta-website:loritta-website-frontend:jsBrowserDistribution")
val jsBrowserProductionWebpack = tasks.getByPath(":loritta-website:loritta-website-frontend:jsBrowserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val sass = tasks.register<SassTask>("sass-style-scss") {
    this.inputSass.set(file("src/main/sass/style.scss"))
    this.inputSassFolder.set(file("src/main/sass/"))
    this.outputSass.set(file("$buildDir/sass/style-scss"))
}

tasks {
    processResources {
        from("../../../resources/") // Include folders from the resources root folder
        from("../../resources/") // Include folders from the resources web folder

        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(jsBrowserDistribution)
        dependsOn(jsBrowserProductionWebpack)
        dependsOn(sass)

        // Copy the output from the frontend task to the backend resources
        from(jsBrowserProductionWebpack.destinationDirectory) {
            into("static/v3/assets/js/")
        }

        // Same thing with the SASS output
        from(sass) {
            into("static/v3/assets/css/")
        }
    }
}