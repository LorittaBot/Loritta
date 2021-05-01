package net.perfectdreams.loritta.platform.interaktions.utils

import dev.kord.common.entity.*
import dev.kord.rest.service.RestClient
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.discord.objects.ChannelType
import net.perfectdreams.loritta.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.discord.objects.LorittaMember
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsGuild
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannelHandler
import net.perfectdreams.loritta.platform.interaktions.entities.StaticInteraKTionsMessageChannel

fun Long.toSnowflake() = Snowflake(this)

fun dev.kord.common.entity.ChannelType.toLorittaChannelType(): ChannelType = when (this) {
    is dev.kord.common.entity.ChannelType.GuildText -> ChannelType.MESSAGE
    is dev.kord.common.entity.ChannelType.DM -> ChannelType.MESSAGE
    else -> ChannelType.VOICE
}

fun DiscordGuildMember.toLorittaMember() = object: LorittaMember {
    override val roles = this@toLorittaMember.roles.map { it.value }
}

fun DiscordGuild.toLorittaGuild(rest: RestClient) = InteraKTionsGuild(
    rest, this
)

fun DiscordChannel.toLorittaChannel(): LorittaDiscordChannel {
    return if (type != dev.kord.common.entity.ChannelType.GuildText) {
        object: LorittaDiscordChannel {
            override val id: Long = this@toLorittaChannel.id.value
            override val type: ChannelType = this@toLorittaChannel.type.toLorittaChannelType()
            override val creation: Instant = this@toLorittaChannel.id.timeStamp.toKotlinInstant()
        }
    } else {
        return StaticInteraKTionsMessageChannel(this)
    }
}

fun DiscordChannel.toLorittaMessageChannel(context: SlashCommandContext) = InteraKTionsMessageChannelHandler(
    this, context
)