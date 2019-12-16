dependencies {
    implementation(kotlin("stdlib"))
    compile(project(":loritta-api"))
    compile("io.github.microutils:kotlin-logging:1.6.26")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    compile("com.github.salomonbrys.kotson:kotson:2.5.0")
}

plugins {
    java
    kotlin("jvm")
    `maven-publish`
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}