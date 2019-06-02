dependencies {
    compile(project(":loritta-core"))
}

plugins {
    java
    kotlin("jvm")
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "net.perfectdreams.loritta.platform.console.ConsoleLoritta",
        mapOf()
)

tasks {
    "build" {
        dependsOn(fatJar)
    }
}