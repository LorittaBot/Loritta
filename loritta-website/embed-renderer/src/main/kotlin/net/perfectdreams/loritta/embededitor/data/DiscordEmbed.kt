package net.perfectdreams.loritta.embededitor.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmbed(
        val title: String? = null,
        val description: String? = null,
        val color: Int? = null,
        val image: EmbedUrl? = null,
        val thumbnail: EmbedUrl? = null,
        val footer: Footer? = null,
        val author: Author? = null,
        val fields: List<Field> = listOf()
) {
    companion object {
        const val MAX_FIELD_OBJECTS = 25
        const val MAX_DESCRIPTION_LENGTH = 2048
        const val MAX_FIELD_NAME_LENGTH = 256
        const val MAX_FIELD_VALUE_LENGTH = 1024
        const val MAX_FOOTER_TEXT_VALUE_LENGTH = 2048
        const val MAX_AUTHOR_NAME_LENGTH = 256
    }

    @Serializable
    data class EmbedUrl(
            val url: String
    )

    @Serializable
    data class Field(
            val name: String,
            val value: String,
            val inline: Boolean = false
    )

    @Serializable
    data class Footer(
            val text: String,
            @SerialName("icon_url")
            val iconUrl: String? = null
    )

    @Serializable
    data class Author(
            val name: String,
            val url: String? = null,
            @SerialName("icon_url")
            val iconUrl: String? = null
    )
}