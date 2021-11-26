plugins {
    kotlin("jvm")
}

// Add maven repositories
repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net")
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":web:web-api-data"))
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha10")
    implementation("io.ktor:ktor-server-netty:1.6.5")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
}

tasks {
    processResources {
        from("../../resources/") // Include folders from the resources root folder
    }
}