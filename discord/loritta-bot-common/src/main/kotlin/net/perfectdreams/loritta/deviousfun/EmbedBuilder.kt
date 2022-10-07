package net.perfectdreams.loritta.deviousfun

import java.awt.Color
import java.time.Instant
import java.time.OffsetDateTime
import java.util.regex.Pattern

class EmbedBuilder {
    companion object {
        val URL_PATTERN: Pattern = Pattern.compile("\\s*(https?|attachment)://\\S+\\s*", Pattern.CASE_INSENSITIVE)
    }

    val content = StringBuilder()
    var title: Title? = null
    var author: Author? = null
    var footer: Footer? = null
    var imageUrl: String? = null
    var thumbnailUrl: String? = null
    var timestamp: Instant? = null
    var color: Int? = null
    val fields = mutableListOf<Field>()

    fun setDescription(text: String?): EmbedBuilder {
        content.clear()
        content.append(text)
        return this
    }

    fun appendDescription(text: String): EmbedBuilder {
        content.append(text)
        return this
    }

    fun setTitle(title: String?, url: String? = null): EmbedBuilder {
        this.title = Title(title, url)
        return this
    }

    fun setAuthor(text: String?, url: String? = null, iconUrl: String? = null): EmbedBuilder {
        this.author = Author(
            text,
            url,
            iconUrl
        )
        return this
    }

    fun setFooter(text: String, iconUrl: String? = null): EmbedBuilder {
        this.footer = Footer(
            text,
            iconUrl
        )
        return this
    }

    fun setImage(imageUrl: String?): EmbedBuilder {
        this.imageUrl = imageUrl
        return this
    }

    fun setThumbnail(thumbnailUrl: String?): EmbedBuilder {
        this.thumbnailUrl = thumbnailUrl
        return this
    }

    fun addField(name: String, value: String, inline: Boolean): EmbedBuilder {
        fields.add(
            Field(
                name,
                value.ifEmpty { "\u200b" }, // If it is empty, replace it with a "fake" empty space
                inline
            )
        )
        return this
    }

    fun setTimestamp(date: OffsetDateTime) = setTimestamp(date.toInstant())

    fun setTimestamp(instant: Instant): EmbedBuilder {
        this.timestamp = instant
        return this
    }

    fun setColor(color: Color?): EmbedBuilder {
        this.color = color?.rgb
        return this
    }

    fun setColor(rawColor: Int): EmbedBuilder {
        this.color = rawColor
        return this
    }

    fun build() = DeviousEmbed(
        content.toString(),
        title,
        author,
        footer,
        fields,
        imageUrl,
        thumbnailUrl,
        timestamp,
        color
    )

    data class Title(
        var title: String?,
        var url: String?
    )

    data class Author(
        var title: String?,
        var url: String?,
        var iconUrl: String?
    )

    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean
    )

    data class Footer(
        val text: String,
        val iconUrl: String?
    )
}