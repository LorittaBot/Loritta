package net.perfectdreams.showtime.backend.content

import kotlinx.serialization.Serializable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class MultilanguageContent(
    val metadata: ContentMetadata,
    val file: File,
    val path: String,
    val localizedContents: Map<String, ContentBase>
) {
    fun getLocalizedVersion(language: String): ContentBase {
        return localizedContents[language] ?: localizedContents["en"] ?: localizedContents["pt"] ?: localizedContents.values.firstOrNull() ?: error("No localized content is present for $file!")
    }

    /**
     * The [MultilanguageContent]'s content metadata
     */
    @Serializable
    data class ContentMetadata(
        val date: String? = null,
        val tags: List<String> = listOf(),
        val imageUrl: String? = null,
        val thumbnailUrl: String? = null,
        val hidden: Boolean = false
    )
}

val MultilanguageContent.ContentMetadata.parsedDate: Date?
    get() {
        if (date == null)
            return null
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date)
    }