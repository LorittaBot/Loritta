dependencies {
    compile(project(":loritta-core"))
    compile("net.perfectdreams.aminoreapi:AminoREAPI:2.0.0-SNAPSHOT")
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

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "net.perfectdreams.loritta.platform.amino.AminoLorittaLauncher",
        mapOf()
)

tasks {
    "build" {
        dependsOn(fatJar)
    }
}