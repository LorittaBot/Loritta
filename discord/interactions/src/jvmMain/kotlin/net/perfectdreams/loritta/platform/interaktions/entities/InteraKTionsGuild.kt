package net.perfectdreams.loritta.platform.interaktions.entities

import dev.kord.common.entity.DiscordGuild
import dev.kord.rest.service.RestClient
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.discord.objects.LorittaGuild
import net.perfectdreams.loritta.discord.objects.LorittaMember
import net.perfectdreams.loritta.platform.interaktions.utils.toLorittaChannel
import net.perfectdreams.loritta.platform.interaktions.utils.toLorittaMember
import net.perfectdreams.loritta.platform.interaktions.utils.toSnowflake

class InteraKTionsGuild(val rest: RestClient, handle: DiscordGuild): LorittaGuild(
    handle.id.value,
    handle.name,
    handle.ownerId.value,
    handle.region,
    handle.id.timeStamp.toKotlinInstant()
) {
    override suspend fun retrieveMember(id: Long): LorittaMember {
        return rest.guild.getGuildMember(this.id.toSnowflake(), id.toSnowflake()).toLorittaMember()
    }

    override suspend fun retrieveChannel(id: Long): LorittaDiscordChannel {
        return rest.channel.getChannel(id.toSnowflake()).toLorittaChannel()
    }

    override suspend fun retrieveChannels(): Collection<LorittaDiscordChannel> {
        return rest.guild.getGuildChannels(this.id.toSnowflake()).map { it.toLorittaChannel() }
    }
}