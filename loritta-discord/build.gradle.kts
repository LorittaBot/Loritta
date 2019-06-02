dependencies {
    compile(project(":loritta-core"))
}

plugins {
    java
    kotlin("jvm")
}

val fatJar = (extra["fat-jar-stuff"] as (String) -> (Task)).invoke("com.mrpowergamerbr.loritta.LorittaLauncher")

tasks {
    "build" {
        dependsOn(fatJar)
    }
}