import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
}

version = Versions.PUDDING

dependencies {
    implementation(project(":common"))
    api(project(":pudding:data"))

    // Databases
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.javatime)
    implementation("org.postgresql:postgresql:42.2.23")
    implementation(libs.hikaricp)
    api("pw.forst", "exposed-upsert", "1.1.0")
    api("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.0.0")
    api("net.perfectdreams.exposedpowerutils:postgres-java-time:1.0.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("ch.qos.logback:logback-classic:1.3.0-alpha14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
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