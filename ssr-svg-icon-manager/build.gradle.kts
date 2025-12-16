plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("net.perfectdreams.harmony.logging:harmonylogging-slf4j:1.0.2")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}