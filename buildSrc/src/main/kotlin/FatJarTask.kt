import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.task
import java.io.File

/**
 * Creates a Fat JAR with a few dependencies inside (`loritta-api`, `loritta-serializable-commons` and `parallax-code-api`)
 *
 * A manifest file is created with the application class path and sets the main class attribute to [mainClass].
 *
 * Custom manifest attributes can be set, or overriden, with the [customAttributes] map
 *
 * So this is just like a "poor mans" Gradle Shadow Plugin. :P
 *
 * @param mainClass        the main class of the application
 * @param customAttributes custom attributes to be set in the manifest
 */
fun Project.fatJarTask(
    runtimeClasspath: Configuration,
    taskProvider: Jar,
    mainClass: String,
    customAttributes: Map<String, String>
): Task {
    return task("fatJar", type = Jar::class) {
        println("Building fat jar for ${project.name}...")
        val addToFinalJarSourceProjects = arrayOf(
            "loritta-api-",
            "loritta-serializable-commons-",
            "parallax-code-api-"
        )

        // Only copy the libs in a "doLast"
        // doLast means that this won't be executed when loading the build.gradle.kts
        // (Yes, by default Gradle will run everything in this task block, even if you are compiling a unrelated project)
        // Very strange...
        doLast {
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
                attributes["Class-Path"] = runtimeClasspath
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
            from(runtimeClasspath.mapNotNull {
                if (addToFinalJarSourceProjects.any { sourceName -> it.name.startsWith(sourceName) }) {
                    zipTree(it)
                } else null
            })

            from(runtimeClasspath.mapNotNull {
                if (!addToFinalJarSourceProjects.any { sourceName -> it.name.startsWith(sourceName) }) {
                    val output = File(libs, it.name)

                    if (it.exists() && !output.exists() && it.extension == "jar")
                        it.copyTo(output, true)
                }
                null
            })
        }

        with(taskProvider as CopySpec)
    }
}