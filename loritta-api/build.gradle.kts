plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}