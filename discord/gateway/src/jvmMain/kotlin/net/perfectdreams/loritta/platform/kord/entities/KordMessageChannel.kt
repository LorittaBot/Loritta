package net.perfectdreams.loritta.platform.kord.entities

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class KordMessageChannel(private val handle: dev.kord.core.entity.channel.MessageChannel) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        val embed = message.embed
        handle.createMessage {
            content = buildString {
                if (message.content != null)
                    append(message.content)

                for (reply in message.replies) {
                    append("\n")
                    append("${reply.prefix} **|** ${reply.content}")
                }
            }

            if (embed != null) {
                embed {
                    title = embed.title
                    description = embed.description
                    image = embed.image
                    thumbnail = embed.thumbnail?.let { url -> EmbedBuilder.Thumbnail().also {it.url = url}}
                    color = embed.color?.let { Color(it.rgb) }
                    timestamp = embed.timestamp?.toJavaInstant()
                    author = embed.author?.toKord()
                    fields = embed.fields.map { it.toKord() }.toMutableList()
                    footer = embed.footer?.toKord()
                }
            }
        }
    }

    private fun LorittaEmbed.Author.toKord() = EmbedBuilder.Author().also {
        it.name = name
        it.icon = icon
        it.url = url
    }

    private fun LorittaEmbed.Field.toKord() = EmbedBuilder.Field().also {
        it.name = name
        it.value = value
        it.inline = inline
    }

    private fun LorittaEmbed.Footer.toKord() = EmbedBuilder.Footer().also {
        it.text = text
        it.icon = icon
    }
}
