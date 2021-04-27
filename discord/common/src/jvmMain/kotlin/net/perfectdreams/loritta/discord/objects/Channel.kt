package net.perfectdreams.loritta.discord.objects

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.pudding.entities.MessageChannel

interface LorittaDiscordChannel {
    val id: Long
    val type: ChannelType
    val creation: Instant

    val asMention: String
        get() = "<#$id>"
}

interface LorittaDiscordMessageChannel: LorittaDiscordChannel, MessageChannel {
    val name: String?
    val topic: String?
    val nsfw: Boolean?
    val guildId: Long?
    override val type: ChannelType
        get() = ChannelType.MESSAGE
}

enum class ChannelType {
    VOICE,
    MESSAGE
}