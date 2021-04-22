package net.perfectdreams.loritta.common.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.utils.embed.LorittaColor

class LorittaEmbed(
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val color: LorittaColor? = null,
    val author: Author? = null,
    val fields: List<Field> = emptyList(),
    val footer: Footer? = null,
    val timestamp: Instant? = null
) {
    class Author(
        val name: String,
        val url: String?,
        val icon: String?
    )
    class Field(
        val name: String,
        val value: String,
        val inline: Boolean
    )
    class Footer(
        val text: String,
        val icon: String?,
    )
}