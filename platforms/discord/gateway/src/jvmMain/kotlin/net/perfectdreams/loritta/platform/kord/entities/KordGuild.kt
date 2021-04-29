package net.perfectdreams.loritta.platform.kord.entities

import dev.kord.core.Kord
import dev.kord.core.Unsafe
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.gateway.DefaultGateway
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.discord.objects.LorittaGuild
import net.perfectdreams.loritta.discord.objects.LorittaMember
import net.perfectdreams.loritta.platform.kord.util.toLorittaChannel
import net.perfectdreams.loritta.platform.kord.util.toLorittaMember
import net.perfectdreams.loritta.platform.kord.util.toSnowflake

class KordGuild(private val client: Kord, private val handle: Guild): LorittaGuild(
    id = handle.id.value,
    name = handle.name,
    ownerId = handle.ownerId.value,
    region = handle.regionId,
    creation = handle.id.timeStamp.toKotlinInstant()
) {
    override suspend fun retrieveMember(id: Long): LorittaMember? {
        return handle.getMemberOrNull(id.toSnowflake())?.toLorittaMember()
    }

    override suspend fun retrieveChannel(id: Long): LorittaDiscordChannel {
        return Channel.from(client.rest.channel.getChannel(id.toSnowflake()).toData(), client).toLorittaChannel()
    }

    override suspend fun retrieveChannels(): Collection<LorittaDiscordChannel> {
        return handle.channels.toList().map {
            it.toLorittaChannel()
        }
    }
}