package net.perfectdreams.loritta.cinnamon.platform.kord.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.MessageChannel
import net.perfectdreams.loritta.cinnamon.discord.objects.ChannelType
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaMember
import net.perfectdreams.loritta.cinnamon.platform.kord.entities.KordGuild
import net.perfectdreams.loritta.cinnamon.platform.kord.entities.KordMessageChannel

fun Long.toSnowflake() = Snowflake(this)

fun dev.kord.common.entity.ChannelType.toLorittaChannelType(): ChannelType = when (this) {
    is dev.kord.common.entity.ChannelType.GuildText -> ChannelType.MESSAGE
    is dev.kord.common.entity.ChannelType.DM -> ChannelType.MESSAGE
    else -> ChannelType.VOICE
}

fun Member.toLorittaMember() = object: LorittaMember {
    override val roles = roleIds.map { it.value }
}

fun Guild.toLorittaGuild(client: Kord) = KordGuild(
    client, this
)

fun Channel.toLorittaChannel(): LorittaDiscordChannel {
    TODO()
    /* return if (this is MessageChannel) toLorittaMessageChannel() else object: LorittaDiscordChannel {
        override val id: Long = data.id.value
        override val type: ChannelType = data.type.toLorittaChannelType()
        override val creation: Instant = data.id.timeStamp
    } */
}

fun MessageChannel.toLorittaMessageChannel() = KordMessageChannel(
    this
)