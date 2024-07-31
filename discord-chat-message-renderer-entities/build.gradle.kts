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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATE_TIME}")

    // Discord
    implementation("com.github.LorittaBot:DeviousJDA:40ea50aea7")
}