package net.perfectdreams.loritta.deviousfun

import java.time.Instant

class DeviousEmbed(
    val description: String,
    val title: EmbedBuilder.Title?,
    val author: EmbedBuilder.Author?,
    val footer: EmbedBuilder.Footer?,
    val fields: MutableList<EmbedBuilder.Field>,
    val image: String?,
    val thumbnail: String?,
    val timestamp: Instant?,
    val color: Int?
) {
    companion object {
        const val URL_MAX_LENGTH = 2000
        const val TITLE_MAX_LENGTH = 256
    }
}