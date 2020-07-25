dependencies {
    api(project(":loritta-discord"))
    api("net.perfectdreams.tradingviewscraper:TradingViewScraper:0.0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
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