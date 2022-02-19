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
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")
    implementation("org.postgresql:postgresql:42.2.23")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("pw.forst", "exposed-upsert", "1.1.0")

    // Required for tests, if this is missing then Gradle will throw
    // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JVM_TARGET
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