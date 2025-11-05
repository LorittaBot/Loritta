import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
}

version = Versions.PUDDING

dependencies {
    implementation(project(":common"))
    api(project(":loritta-serializable-commons"))
    implementation(project(":loritta-dashboard:loritta-shimeji-common"))

    // Databases
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.javatime)
    api(libs.postgresqljdbcdriver)
    implementation(libs.hikaricp)
    api("net.perfectdreams.exposedpowerutils:exposed-power-utils:1.2.1")
    api("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.2.1")
    api("net.perfectdreams.exposedpowerutils:postgres-java-time:1.2.1")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation(libs.logback.classic)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

tasks.withType<Test> {
    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    useJUnitPlatform()
}

// Required due to the "enforced platform" implementation, we want to ignore this error
tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.perfectdreams.loritta.cinnamon.pudding"
            artifactId = "client"
            version = Versions.PUDDING

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")
            credentials(PasswordCredentials::class)
        }
    }
}