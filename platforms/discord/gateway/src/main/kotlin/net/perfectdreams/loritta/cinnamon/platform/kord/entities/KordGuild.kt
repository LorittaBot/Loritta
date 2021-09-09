package net.perfectdreams.loritta.cinnamon.platform.kord.entities

import dev.kord.core.Kord
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.flow.toList
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaDiscordChannel
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaGuild
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaMember
import net.perfectdreams.loritta.cinnamon.platform.kord.util.toLorittaChannel
import net.perfectdreams.loritta.cinnamon.platform.kord.util.toLorittaMember
import net.perfectdreams.loritta.cinnamon.platform.kord.util.toSnowflake

class KordGuild(private val client: Kord, private val handle: Guild): LorittaGuild(
    id = handle.id.value,
    name = handle.name,
    ownerId = handle.ownerId.value,
    region = handle.regionId,
    creation = handle.id.timeStamp
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