dependencies {
    compile(project(":loritta-core"))
}

plugins {
    java
    kotlin("jvm")
}

val fatJar = (extra["fat-jar-stuff"] as (String) -> (Task)).invoke("net.perfectdreams.loritta.platform.console.ConsoleLoritta")

tasks {
    "build" {
        dependsOn(fatJar)
    }
}