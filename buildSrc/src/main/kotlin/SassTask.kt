
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Converts a SASS file to a CSS file and puts the converted file into `build/sass/[outputSass]`
 *
 * The Dart Sass implementation will be downloaded, based on your current operating system, after that, your SASS file will be converted to CSS.
 *
 * Don't forget to include the generated CSS file into your resources!
 *
 * ```
 * from(sassTask) {
 *   into("static/v3/assets/css/")
 * }
 * ```
 *
 * This is just a hacky workaround, because the Gradle task we were using before isn't supported in Gradle 7, however this is a nice workaround that seems to work fine :)
 *
 * @param inputSass the file that will be converted
 */
@CacheableTask
abstract class SassTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val inputSass: RegularFileProperty

    // Actually this is not used, but it is used to trigger a rebuild if any of the files were changed
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val inputSassFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSass: DirectoryProperty

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val operatingSystem = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()

        val isWindows = operatingSystem.internalOs.isWindows
        val isLinux = operatingSystem.internalOs.isLinux

        val folderName = when {
            isWindows -> "windows"
            isLinux -> "linux"
            else -> throw UnsupportedOperationException("Unsupported OS $operatingSystem! The sassTask code must be updated to support it!")
        }

        val url = when {
            isWindows -> "https://github.com/sass/dart-sass/releases/download/1.35.2/dart-sass-1.35.2-windows-x64.zip"
            isLinux -> "https://github.com/sass/dart-sass/releases/download/1.35.2/dart-sass-1.35.2-linux-x64.tar.gz"
            else -> throw UnsupportedOperationException("Unsupported OS $operatingSystem! The sassTask code must be updated to support it!")
        }

        // The "caches" folder is used by Paperweight, so that's why I used the same name
        val dartSassTempFolder = File(project.rootDir, ".gradle/caches/dart-sass")
        dartSassTempFolder.mkdirs()

        val dartSassOsTempFolder = File(dartSassTempFolder, folderName)

        if (!dartSassOsTempFolder.exists()) {
            dartSassOsTempFolder.mkdirs()
            println("Downloading SASS from $url... Hang tight!")
            val sass = URL(url).readBytes()

            val extension = if (url.endsWith(".zip"))
                "zip"
            else
                "tar.gz"

            // Write the file...
            val sassZipFile = File(dartSassTempFolder, "sass.$extension")
            sassZipFile.writeBytes(sass)

            // And then extract it!
            if (extension == "zip") {
                extractZip(sassZipFile, dartSassOsTempFolder)
            } else {
                extractTarGz(sassZipFile, dartSassOsTempFolder)
            }
        }

        // Execute SASS
        val originalSassLocation = inputSass.get().asFile
        val outputSassLocation = outputSass.get().asFile
        val outputSassLocationFile = File(outputSassLocation, originalSassLocation.nameWithoutExtension + ".css")

        when {
            isWindows -> {
                val pb = ProcessBuilder(
                    "cmd.exe",
                    "/C",
                    "$dartSassOsTempFolder\\dart-sass\\sass.bat",
                    originalSassLocation.toString(),
                    outputSassLocationFile.toString()
                )
                    .also {
                        println(it.command())
                    }
                    .start()

                val status = pb.waitFor()

                println(pb.inputStream.readAllBytes().toString(Charsets.UTF_8))
                println(pb.errorStream.readAllBytes().toString(Charsets.UTF_8))

                println("Process Status: $status")

                if (status != 0)
                    error("SASS failed! Status: $status")
            }
            isLinux -> {
                val pb = ProcessBuilder(
                    "$dartSassOsTempFolder/dart-sass/sass",
                    originalSassLocation.toString(),
                    outputSassLocationFile.toString()
                )
                    .start()

                val status = pb.waitFor()

                println(pb.inputStream.readAllBytes().toString(Charsets.UTF_8))
                println(pb.errorStream.readAllBytes().toString(Charsets.UTF_8))

                println("Process Status: $status")

                if (status != 0)
                    error("SASS failed! Status: $status")
            }
            else -> {
                throw UnsupportedOperationException("Unsupported OS $operatingSystem! The sassTask code must be updated to support it!")
            }
        }

        println("Done!")
    }


    private fun extractZip(input: File, output: File) {
        ZipFile(input).use { zipFile ->
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry = entries.nextElement() ?: return
                val entryDestination = File(output, entry.name)
                if (entry.isDirectory) {
                    entryDestination.mkdirs()
                } else {
                    entryDestination.parentFile.mkdirs()
                    zipFile.getInputStream(entry).use { `in` ->
                        FileOutputStream(entryDestination).use { out ->
                            zipFile.getInputStream(entry).transferTo(out)
                        }
                    }
                }
            }
        }
    }

    private fun extractTarGz(input: File, output: File) {
        // Pray that the user has sh and tar installed
        val builder = ProcessBuilder()
        builder.command("sh", "-c", java.lang.String.format("tar xfz %s -C %s", input, output))
        builder.directory(File("/tmp"))
        val process = builder.start()
        process.waitFor()
    }
}