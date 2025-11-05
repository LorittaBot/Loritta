plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING}")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation(libs.ktor.client.java)
    implementation(libs.kotlinx.serialization.json)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

tasks.test {
    useJUnitPlatform()
}