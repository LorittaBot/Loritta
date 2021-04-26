import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories { jcenter() }
}

allprojects {
    group = "net.perfectdreams.loritta"
    version = Versions.LORITTA

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://dl.bintray.com/kotlin/kotlin-dev/")
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
        maven("https://dl.bintray.com/kotlin/kotlinx.html")
        maven("https://jcenter.bintray.com")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://dl.bintray.com/kotlin/ktor/")
        maven("https://jitpack.io")
        maven("https://m2.dv8tion.net/releases")
    }
}

plugins {
    java
    kotlin("jvm") version "1.4.10" apply false
    `maven-publish`
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
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
