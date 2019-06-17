dependencies {
    implementation(kotlin("stdlib"))
    compile(project(":loritta-api"))
    compile("io.github.microutils:kotlin-logging:1.6.26")
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