import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// val loriVersion by lazy { ext["lori-version"] as String }
// val kotlinVersion by lazy { ext["kotlin-version"] as String }
// val ktorVersion by lazy { ext["ktor-version"] as String }
val loriVersion   = "2019.11.01-SNAPSHOT"
val kotlinVersion = "1.3.31"
val ktorVersion   = "1.2.1"
val jdaVersion    = "4.0.0_45"

println("Compiling Loritta $loriVersion")
println("Kotlin Version: $kotlinVersion")

buildscript {
    repositories { jcenter() }
}

allprojects {
    extra.apply {
        set("lori-version", loriVersion)
        set("kotlin-version", kotlinVersion)
        set("ktor-version", ktorVersion)
        set("jda-version", jdaVersion)
        set(
                "fat-jar-stuff",
                fun(mainClass: String, customAttributes: Map<String, String>): Task {
                    return task("fatJar", type = Jar::class) {
                        println("Building fat jar for ${project.name}...")

                        archiveBaseName.set("${project.name}-fat")

                        manifest {
                            fun addIfAvailable(name: String, attrName: String) {
                                attributes[attrName] = System.getProperty(name) ?: "Unknown"
                            }

                            attributes["Loritta-Version"] = loriVersion
                            addIfAvailable("build.number", "Build-Number")
                            addIfAvailable("commit.hash", "Commit-Hash")
                            addIfAvailable("git.branch", "Git-Branch")
                            addIfAvailable("compiled.at", "Compiled-At")
                            attributes["Main-Class"] = mainClass
                            attributes["Kotlin-Version"] = kotlinVersion
                            attributes["Class-Path"] = configurations.compile.get().joinToString(" ", transform = { "libs/" + it.name })
                            attributes.putAll(customAttributes)
                        }

                        val libs = File(rootProject.projectDir, "libs")
                        // libs.deleteRecursively()
                        libs.mkdirs()

                        from(configurations.runtimeClasspath.get().mapNotNull {
                            if (it.name.startsWith("loritta-core-") || it.name.startsWith("loritta-api-")) {
                                zipTree(it)
                            } else {
                                val output = File(libs, it.name)

                                if (it.exists() && !output.exists())
                                    it.copyTo(output, true)

                                null
                            }
                        })

                        with(tasks.jar.get() as CopySpec)
                    }
                }
        )
    }

    group = "net.perfectdreams.loritta"
    version = loriVersion

    repositories {
        mavenLocal()

        maven("https://dl.bintray.com/kotlin/kotlin-dev/")
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
        maven("http://jcenter.bintray.com")
        maven("https://raw.githubusercontent.com/JRakNet/MavenRepository/master")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://dl.bintray.com/kotlin/ktor/")
        maven("https://jitpack.io")
    }
}

plugins {
    java
    kotlin("jvm") version "1.3.31"
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
}
