plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(project(":discord-oauth2"))
    implementation(project(":luna:bliss"))
    implementation(project(":luna:toast-common"))
    implementation(project(":luna:modal-common"))
    implementation(project(":discord-chat-markdown-parser"))
    implementation(project(":ssr-svg-icon-manager"))
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.client.java)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
    implementation(libs.hikaricp)
    implementation(libs.postgresqljdbcdriver)
    implementation(libs.kotlinx.coroutines.core)
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.yaml:snakeyaml:2.5")
    implementation("ch.qos.logback:logback-classic:1.5.22")
    implementation("net.perfectdreams.harmony.logging:harmonylogging-slf4j:1.0.2")
    implementation("commons-codec:commons-codec:1.20.0")
}

val sass = tasks.register<SassTask>("sassStyleScss") {
    this.inputSass.set(file("src/main/sass/style.scss"))
    this.inputSassFolder.set(file("src/main/sass/"))
    this.outputSass.set(file("$buildDir/sass/style-scss"))
}

val skipFrontendBundle = (findProperty("net.perfectdreams.dora.skipFrontendDistribution") as String?)?.toBoolean() == true
val frontendJsBundle = tasks.getByPath(":dora:frontend:jsBrowserProductionWebpack")

tasks {
    processResources {
        dependsOn(sass)

        from(sass) {
            into("dashboard/css/")
        }

        if (!skipFrontendBundle) {
            from(frontendJsBundle) {
                into("dashboard/js/")
            }
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

jib {
    container {
        mainClass = "net.perfectdreams.dora.DoraBackendLauncher"
    }

    to {
        image = "ghcr.io/lorittabot/dora-backend"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker-dora" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "tar://${File(rootDir, "docker-dora/image.tar").absoluteFile}"
    }
}