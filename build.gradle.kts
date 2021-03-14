import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories { jcenter() }
}

allprojects {
    extra.apply {
        set(
                "fat-jar-stuff",
                fun(mainClass: String, customAttributes: Map<String, String>): Task {
                    return task("fatJar", type = Jar::class) {
                        println("Building fat jar for ${project.name}...")
                        val addToFinalJarSourceProjects = arrayOf(
                                "loritta-api-",
                                "loritta-serializable-commons-",
                                "parallax-code-api-"
                        )

                        archiveBaseName.set("${project.name}-fat")

                        manifest {
                            fun addIfAvailable(name: String, attrName: String) {
                                attributes[attrName] = System.getProperty(name) ?: "Unknown"
                            }

                            attributes["Loritta-Version"] = Versions.LORITTA
                            addIfAvailable("build.number", "Build-Number")
                            addIfAvailable("commit.hash", "Commit-Hash")
                            addIfAvailable("git.branch", "Git-Branch")
                            addIfAvailable("compiled.at", "Compiled-At")
                            addIfAvailable("github.build.id", "Github-Build-Id")
                            attributes["Main-Class"] = mainClass
                            attributes["Kotlin-Version"] = Versions.KOTLIN
                            attributes["Class-Path"] = configurations.runtimeClasspath.get()
                                    .filterNot { addToFinalJarSourceProjects.any { sourceName -> it.name.startsWith(sourceName) } }
                                    .filter { it.extension == "jar" }
                                    .distinctBy { it.name }
                                    .joinToString(" ", transform = { "libs/" + it.name })
                            attributes.putAll(customAttributes)
                        }

                        val libs = File(rootProject.projectDir, "libs")
                        // libs.deleteRecursively()
                        libs.mkdirs()

                        // Add any required dependencies inside the JAR
                        from(configurations.runtimeClasspath.get().mapNotNull {
                            if (addToFinalJarSourceProjects.any { sourceName -> it.name.startsWith(sourceName) }) {
                                zipTree(it)
                            } else null
                        })

                        doLast {
                            // Only copy the libs in a "doLast"
                            // doLast means that this won't be executed when loading the build.gradle.kts
                            // (Yes, by default Gradle will run everything in this task block, even if you are compiling a unrelated project)
                            // Very strange...
                            from(configurations.runtimeClasspath.get().mapNotNull {
                                if (!addToFinalJarSourceProjects.any { sourceName -> it.name.startsWith(sourceName) }) {
                                    val output = File(libs, it.name)

                                    if (it.exists() && !output.exists() && it.extension == "jar")
                                        it.copyTo(output, true)
                                }
                                null
                            })
                        }

                        with(tasks.jar.get() as CopySpec)
                    }
                }
        )
    }

    group = "net.perfectdreams.loritta"
    version = Versions.LORITTA

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://dl.bintray.com/kotlin/kotlin-dev/")
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
        maven("https://dl.bintray.com/kotlin/kotlinx.html")
        maven("https://jcenter.bintray.com")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://dl.bintray.com/kotlin/ktor/")
        maven("https://jitpack.io")
    }
}

plugins {
    java
    kotlin("jvm") version "1.4.10" apply false
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

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
}
