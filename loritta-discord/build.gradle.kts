val jdaVersion by lazy { ext["jda-version"] as String }

dependencies {
    compile(project(":loritta-core"))
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