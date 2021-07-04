package net.perfectdreams.loritta.platform.interaktions.utils

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordGuild
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.context.commands.SlashCommandContext
import net.perfectdreams.loritta.discord.objects.ChannelType
import net.perfectdreams.loritta.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.discord.objects.LorittaMember
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsGuild
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannelHandler

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
    TODO()
    /* return if (type != dev.kord.common.entity.ChannelType.GuildText) {
        object: LorittaDiscordChannel {
            override val id: Long = this@toLorittaChannel.id.value
            override val type: ChannelType = this@toLorittaChannel.type.toLorittaChannelType()
            override val creation: Instant = this@toLorittaChannel.id.timeStamp.toKotlinInstant()
        }
    } else {
        return StaticInteraKTionsMessageChannel(this)
    } */
}

fun DiscordChannel.toLorittaMessageChannel(context: SlashCommandContext) = InteraKTionsMessageChannelHandler(
    TODO()
    // this, context
)