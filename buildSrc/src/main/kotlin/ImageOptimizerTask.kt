
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject


// @CacheableTask
abstract class ImageOptimizerTask : DefaultTask() {
    companion object {
        // https://medium.com/hceverything/applying-srcset-choosing-the-right-sizes-for-responsive-images-at-different-breakpoints-a0433450a4a3
        // https://cloudfour.com/thinks/responsive-images-the-simple-way/#what-image-sizes-should-i-provide
        val widthSizes = listOf(
            80,
            160,
            320,
            640,
            960,
            1280,
            1920,
            2560
        )
    }

    @get:Incremental
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val sourceImagesDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputImagesInfoFile: RegularFileProperty

    @get:Input
    abstract val imagesOptimizationSettings: ListProperty<ImageOptimizationSettings>

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val workQueue = workerExecutor.noIsolation()

        val outputImagesInfoFile = outputImagesInfoFile.get().asFile
        val targetFolder = outputImagesDirectory.asFile.get()
        val pngQuantPath = findPngQuantCommandPath()
        val gifsiclePath = findGifsicleCommandPath()

        val list = if (outputImagesInfoFile.exists())
            CopyOnWriteArrayList(Json.decodeFromString(ListSerializer(ImageInfo.serializer()),  outputImagesInfoFile.readText()))
        else
            CopyOnWriteArrayList()

        println(
            if (inputChanges.isIncremental) "Executing incrementally"
            else "Executing non-incrementally"
        )

        inputChanges.getFileChanges(sourceImagesDirectory).forEachIndexed { index, change ->
            if (change.fileType == FileType.DIRECTORY) return@forEachIndexed

            println("${change.changeType}: ${change.normalizedPath}")
            val targetFile = outputImagesDirectory.file(change.normalizedPath).get().asFile
            if (change.changeType == ChangeType.REMOVED) {
                val targetFileRelativeToTheBaseFolder = targetFile.relativeTo(targetFolder)

                targetFile.delete()

                for (newWidth in widthSizes) {
                    File(targetFile.parentFile, targetFile.nameWithoutExtension + "_${newWidth}w.png")
                        .delete()
                }

                list.removeIf { it.path == targetFileRelativeToTheBaseFolder.toString().replace("\\", "/") }
            } else {
                workQueue.submit(GenerateOptimizedImage::class) {
                    this.fileIndex.set(index)
                    this.sourceFile.set(change.file)
                    this.targetFile.set(targetFile)
                    this.targetFolder.set(targetFolder)
                    this.temporaryFolder.set(temporaryDir)
                    this.pngQuantPathString.set(pngQuantPath)
                    this.gifsiclePathString.set(gifsiclePath)
                }
            }
        }

        workQueue.await()

        // Now we need to load all JSON files in the temporary directory and append them all into a single list
        val imagesInfo = temporaryDir.listFiles().flatMap {
            Json.decodeFromString(ListSerializer(ImageInfo.serializer()), it.readText())
        }

        outputImagesInfoFile
            .writeText(Json.encodeToString(ListSerializer(ImageInfo.serializer()), imagesInfo))
    }

    private fun findPngQuantCommandPath() = findAppCommandPath("pngquant")
    private fun findGifsicleCommandPath() = findAppCommandPath("gifsicle")

    private fun findAppCommandPath(app: String): String {
        logger.info("Finding where $app is...")

        try {
            ProcessBuilder(
                app,
            ).start()

            return app
        } catch (e: IOException) {
            logger.info("$app was not found in the path \"$app\"...")
        }

        try {
            ProcessBuilder(
                "$app.exe",
            ).start()

            return "$app.exe"
        } catch (e: IOException) {
            logger.info("$app was not found in the path \"$app.exe\"...")
        }

        try {
            ProcessBuilder(
                "/usr/bin/$app",
            ).start()

            return "/usr/bin/$app"
        } catch (e: IOException) {
            logger.info("PNGQuant was not found in the path \"/usr/bin/$app\"...")
        }

        val systemPropPngQuantPath = System.getProperty("$app.path")

        if (systemPropPngQuantPath != null) {
            try {
                ProcessBuilder(
                    systemPropPngQuantPath,
                ).start()

                return systemPropPngQuantPath
            } catch (e: IOException) {
                logger.info("$app was not found in the path \"$systemPropPngQuantPath\"...")
            }
        }

        error("$app was not found in the path! Please install $app or, if it is already installed, provide the path via the \"$app.path\" system property (Example: \"./gradlew -D$app.path=/home/lorittapath/to/$app/$app ...\")")
    }
}

