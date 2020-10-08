dependencies {
    api(project(":loritta-discord"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("io.ktor:ktor-client-websockets:1.4.1")
}

plugins {
    java
    kotlin("jvm")
    `maven-publish`
}

tasks {
    val fatJar = task("fatJar", type = Jar::class) {
        println("Building fat jar for ${project.name}...")

        archiveBaseName.set("${project.name}-fat")

        from(configurations.runtimeClasspath.get().mapNotNull {
            if (it.name.contains("TradingViewScraper"))
                zipTree(it)
            else
                null
        })

        with(jar.get() as CopySpec)
    }

    "build" {
        dependsOn(fatJar)
    }
}