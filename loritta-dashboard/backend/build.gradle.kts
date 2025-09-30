plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.4.5"
}

dependencies {
    implementation("io.ktor:ktor-server-cio:3.2.3")
    implementation("io.ktor:ktor-server-html-builder:3.2.3")

    implementation("io.ktor:ktor-client-cio:3.2.3")
}

val sass = tasks.register<SassTask>("sassStyleScss") {
    this.sassVersion.set("1.93.2")
    this.inputSass.set(file("src/main/sass/style.scss"))
    this.inputSassFolder.set(file("src/main/sass/"))
    this.outputSass.set(file("${layout.buildDirectory.get()}/sass/style-scss"))
}

tasks {
    processResources {
        // Same thing with the SASS output
        from(sass) {
            into("static/v2/assets/css/")
        }
    }
}