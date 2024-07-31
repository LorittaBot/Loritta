plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":discord-chat-markdown-parser"))
    api(project(":discord-chat-message-renderer-entities"))

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.KOTLIN_COROUTINES}")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATE_TIME}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.7.1")

    // Discord
    implementation("com.github.LorittaBot:DeviousJDA:40ea50aea7")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.10.1")
    implementation("com.microsoft.playwright:playwright:1.45.0")

    implementation(libs.kotlin.logging)
}

val sassMessageRenderer = tasks.register<SassTask>("sass-message-renderer") {
    this.inputSass.set(file("src/main/sass-message-renderer/style.scss"))
    this.inputSassFolder.set(file("src/main/sass-message-renderer/"))
    this.outputSass.set(file("$buildDir/sass/sass-message-renderer-scss"))
}

tasks {
    processResources {
        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(sassMessageRenderer)

        // Same thing with the SASS output
        from(sassMessageRenderer) {
            into("message-renderer-assets/")
        }
    }
}

