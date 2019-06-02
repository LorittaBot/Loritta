dependencies {
    compile(project(":loritta-core"))
    compile("net.perfectdreams.aminoreapi:AminoREAPI:2.0.0-SNAPSHOT")
}

plugins {
    java
    kotlin("jvm")
}

val fatJar = (extra["fat-jar-stuff"] as (String) -> (Task)).invoke("net.perfectdreams.loritta.platform.amino.AminoLorittaLauncher")

tasks {
    "build" {
        dependsOn(fatJar)
    }
}