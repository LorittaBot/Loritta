package net.perfectdreams.loritta.cinnamon.platform.utils.parallax

import dev.kord.common.Color
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.serialization.Serializable
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl

@Serializable
data class ParallaxMessage(
    val content: String? = null,
    val embeds: List<ParallaxEmbed> = listOf()
) {
    fun apply(builder: UserMessageCreateBuilder) {
        builder.content = content

        for (embed in embeds) {
            builder.embed {
                this.title = embed.title
                this.description = embed.description
                this.url = embed.url
                this.color = embed.color?.let { Color(it) }
                embed.author?.let {
                    author(it.name, it.url, it.iconUrl)
                }
                embed.footer?.let {
                    footer(it.text, it.iconUrl)
                }
                this.image = embed.image?.url
                this.thumbnailUrl = embed.thumbnail?.url
                embed.fields.forEach {
                    field(it.name, it.value, it.inline)
                }
            }
        }
    }
}