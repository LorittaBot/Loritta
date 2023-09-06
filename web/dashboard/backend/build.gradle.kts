plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":web:dashboard:dashboard-common"))
    implementation(project(":pudding:client"))
    implementation(project(":temmie-discord-auth"))

    // Logging Stuff
    implementation(libs.logback.classic)

    // Logback GELF, used for Graylog logging
    implementation("de.siegmar:logback-gelf:3.0.0")

    implementation("commons-codec:commons-codec:1.15")

    // Ktor
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-html-builder:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-cors:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-sessions:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")

    // Sequins
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/spicy-morenitta"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:17-focal"
    }
}

// DEV VERSION:
val jsBrowserDistribution = tasks.getByPath(":web:dashboard:spicy-frontend:jsDevelopmentExecutableCompileSync")
// I don't know why this is required when developing
val jsBrowserDistributionX = tasks.getByPath(":web:dashboard:spicy-frontend:jsProductionExecutableCompileSync")
jsBrowserDistributionX.dependsOn(jsBrowserDistribution)
val jsBrowserProductionWebpack = tasks.getByPath(":web:dashboard:spicy-frontend:jsBrowserDevelopmentWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
// PROD VERSION:
// val jsBrowserDistribution = tasks.getByPath(":web:dashboard:spicy-frontend:jsBrowserDistribution")
// val jsBrowserProductionWebpack = tasks.getByPath(":web:dashboard:spicy-frontend:jsBrowserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

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
            into("static/assets/js/")
        }

        // Same thing with the SASS output
        from(sass) {
            into("static/assets/css/")
        }
    }
}