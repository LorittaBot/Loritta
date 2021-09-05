package net.perfectdreams.loritta.platform.interaktions.utils

import dev.kord.common.entity.DiscordGuild
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.discord.objects.ChannelType
import net.perfectdreams.loritta.discord.objects.LorittaMember
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsGuild

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