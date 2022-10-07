package net.perfectdreams.loritta.deviousfun.events.message.delete

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class MessageBulkDeleteEvent(
    jda: JDA,
    gateway: DeviousGateway,
    val messageIdsSnowflakes: List<Snowflake>,
    val channel: Channel,
    val guild: Guild?
) : Event(jda, gateway) {
    val messageIds: List<String>
        get() = messageIdsSnowflakes.map { it.toString() }

    val channelIdSnowflake: Snowflake
        get() = channel.idSnowflake
    val channelId: String
        get() = channelIdSnowflake.toString()
    val channelIdLong: Long
        get() = channelIdSnowflake.toLong()
}