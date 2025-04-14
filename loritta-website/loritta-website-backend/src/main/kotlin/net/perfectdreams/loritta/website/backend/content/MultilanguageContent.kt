package net.perfectdreams.loritta.website.backend.content

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.io.File

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
     * Checks if a content should be displayed based on its properties
     *
     * The content should be displayed if:
     * * The [hidden] flag is not set to true
     * * The [date] is older than the current time
     */
    fun shouldBeDisplayedInPostList() = !metadata.hidden && Clock.System.now() > metadata.date

    /**
     * The [MultilanguageContent]'s content metadata
     */
    @Serializable
    data class ContentMetadata(
        val date: Instant,
        val tags: List<String> = listOf(),
        val imageUrl: String? = null,
        val thumbnailUrl: String? = null,
        val hidden: Boolean = false
    )
}