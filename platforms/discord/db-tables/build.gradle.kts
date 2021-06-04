import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

version = "0.0.1-SNAPSHOT" // Bump when the schema changes!

dependencies {
    // Re-enable this if any of the tables requires stuff in the common module!
    // However keep in mind that if this is re-enabled, the common module needs to be published!
    // api(project(":common"))
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    api("org.jetbrains.exposed:exposed-dao:${Versions.EXPOSED}")
    api("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
}

publishing {
    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")

            credentials {
                username = System.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = System.getProperty("PASSWORD") ?: System.getenv("PASSWORD")
            }
        }
    }

    publications {
        register("PerfectDreams", MavenPublication::class.java) {
            from(components["java"])
        }
    }
}