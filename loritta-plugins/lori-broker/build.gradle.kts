dependencies {
    api(project(":platforms:discord:legacy"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
    implementation("io.ktor:ktor-client-websockets:${Versions.KTOR}")
}

plugins {
    java
    kotlin("jvm")
}

tasks {
    val fatJar = task("fatJar", type = Jar::class) {
        doFirst {
            archiveBaseName.set("${project.name}-fat")

            from(configurations.runtimeClasspath.get().mapNotNull {
                if (it.name.contains("TradingViewScraper"))
                    zipTree(it)
                else
                    null
            })
        }

        with(jar.get() as CopySpec)
    }

    "build" {
        dependsOn(fatJar)
    }
}