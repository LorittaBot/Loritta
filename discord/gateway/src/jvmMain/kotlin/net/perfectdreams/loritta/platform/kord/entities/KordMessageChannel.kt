package net.perfectdreams.loritta.platform.kord.entities

import dev.kord.common.Color
import dev.kord.common.entity.optional.value
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel

class KordMessageChannel(private val handle: dev.kord.core.entity.channel.MessageChannel) : LorittaDiscordMessageChannel {
    override val id: Long = handle.id.value
    override val name: String? = handle.data.name.value
    override val topic: String? = handle.data.topic.value
    override val nsfw: Boolean? = handle.data.nsfw.value
    override val guildId: Long? = handle.data.guildId.value?.value
    override val creation: Instant = handle.id.timeStamp.toKotlinInstant()

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