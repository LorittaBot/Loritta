val jdaVersion by lazy { ext["jda-version"] as String }

dependencies {
    compile(project(":loritta-core"))
}

plugins {
    java
    kotlin("jvm")
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
        "com.mrpowergamerbr.loritta.LorittaLauncher",
        mapOf(
                "JDA-Version" to jdaVersion
        )
)

tasks {
    "build" {
        dependsOn(fatJar)
    }
}