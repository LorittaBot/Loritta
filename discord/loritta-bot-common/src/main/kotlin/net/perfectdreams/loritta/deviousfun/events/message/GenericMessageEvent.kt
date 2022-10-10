package net.perfectdreams.loritta.deviousfun.events.message

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

abstract class GenericMessageEvent(
    deviousFun: DeviousFun,
    gateway: DeviousGateway,
    val messageIdSnowflake: Snowflake,
    val channel: Channel
) : Event(deviousFun, gateway) {
    val messageId: String
        get() = messageIdSnowflake.toString()
    val messageIdLong: Long
        get() = messageIdSnowflake.toLong()

    val channelIdSnowflake: Snowflake
        get() = channel.idSnowflake
    val channelId: String
        get() = channelIdSnowflake.toString()
    val channelIdLong: Long
        get() = channelIdSnowflake.toLong()
}