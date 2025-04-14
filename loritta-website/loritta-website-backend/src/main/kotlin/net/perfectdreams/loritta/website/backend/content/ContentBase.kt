package net.perfectdreams.loritta.website.backend.content

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File

class ContentBase private constructor(
    val file: File,
    val languageId: String,
    val path: String,
    val metadata: LanguageSpecificContentMetadata,
    val content: String
) {
    companion object {
        fun fromFile(file: File, path: String): ContentBase {
            val entireTextFile = file.readText().removePrefix("---") // Because of YAML front matter
            val metadataHeader = entireTextFile.substringBefore("---")
            val content = entireTextFile.substringAfter("---")

            return ContentBase(
                file,
                file.nameWithoutExtension,
                path,
                Yaml.default.decodeFromString(metadataHeader),
                content
            )
        }
    }

    /**
     * The [ContentBase]'s content metadata, containing language specific metadata
     */
    @Serializable
    data class LanguageSpecificContentMetadata(
        val title: String,
        val description: String? = null
    )
}