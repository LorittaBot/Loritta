dependencies {
    implementation(kotlin("stdlib"))
    compile("io.github.microutils:kotlin-logging:1.6.26")
    compile("com.github.salomonbrys.kotson:kotson:2.5.0")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
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