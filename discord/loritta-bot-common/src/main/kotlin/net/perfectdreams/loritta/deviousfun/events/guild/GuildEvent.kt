package net.perfectdreams.loritta.deviousfun.events.guild

import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

open class GuildEvent(deviousShard: DeviousShard, gateway: DeviousGateway, val guild: Guild) : Event(deviousShard, gateway) {
    val guildIdSnowflake by guild::idSnowflake
    val guildId: String
        get() = guildIdSnowflake.toString()
    val guildIdLong: Long
        get() = guildIdSnowflake.toLong()
}