plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
    api("org.jsoup:jsoup:1.21.2")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}