import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":loritta-bot-discord"))
    implementation(project(":loritta-serializable-commons"))
    implementation(project(":pudding:client"))
    // Discord
    implementation("com.github.LorittaBot:DeviousJDA:c98147549f")
    implementation("club.minnced:jda-ktx:0.12.0")
    // DreamStorageService
    implementation("net.perfectdreams.dreamstorageservice:client:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATE_TIME}")
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