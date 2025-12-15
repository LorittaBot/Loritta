plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.java)
    implementation("net.perfectdreams.harmony.logging:harmonylogging-slf4j:1.0.2")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}