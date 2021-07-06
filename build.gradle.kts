import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories { jcenter() }
}

allprojects {
    group = "net.perfectdreams.loritta"
    version = Versions.LORITTA

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")

        // Used by kotlinx.html, can be removed after migrating to the newest kotlinx.html version
        maven("https://plugins.gradle.org/m2")
        maven("https://repo.perfectdreams.net/")
        maven("https://jitpack.io")

        // Used by JDA
        maven("https://m2.dv8tion.net/releases")
    }
}

plugins {
    java
    kotlin("jvm") version Versions.KOTLIN apply false
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
}
