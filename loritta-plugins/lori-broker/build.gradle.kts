val ktorVersion by lazy { ext["ktor-version"] as String }

dependencies {
    api(project(":loritta-discord"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
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