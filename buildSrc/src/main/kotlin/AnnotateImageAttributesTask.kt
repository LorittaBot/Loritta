
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import javax.imageio.ImageIO

/**
 * Reads image files and writes their attributes (path, width, height, file size) to a JSON file
 */
abstract class AnnotateImageAttributesTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val sourceImagesDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputImagesInfoFile: RegularFileProperty

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val sourceImagesDirectory = sourceImagesDirectory.get().asFile
        val outputImagesInfoFile = outputImagesInfoFile.get().asFile

        val list = CopyOnWriteArrayList<ImageInfo>()

        val allFiles = Files.walk(sourceImagesDirectory.toPath())
            .filter(Files::isRegularFile)
            .collect(Collectors.toList())
            .map {
                it.toFile()
            }

        allFiles.forEach {
            if (it.extension in listOf("png", "jpg", "jpeg", "gif")) {
                val relativeFile = it.relativeTo(sourceImagesDirectory)

                val fileSize = it.length()
                val image = ImageIO.read(it)

                logger.quiet(
                    "File ${
                        relativeFile.toString().replace("\\", "/")
                    } is ${image.width}x${image.height} and it is $fileSize bytes"
                )

                list.add(
                    ImageInfo(
                        relativeFile.toString().replace("\\", "/"),
                        image.width,
                        image.height,
                        fileSize,
                        listOf()
                    )
                )
            }
        }

        outputImagesInfoFile
            .writeText(Json.encodeToString(ListSerializer(ImageInfo.serializer()), list))
    }
}

