
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface GenerateOptimizedImageParameters : WorkParameters {
    val fileIndex: Property<Int>
    val sourceFile: RegularFileProperty
    val temporaryFolder: RegularFileProperty
    val targetFile: RegularFileProperty
    val targetFolder: RegularFileProperty
    val pngQuantPathString: Property<String>
    val gifsiclePathString: Property<String>
}