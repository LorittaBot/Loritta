package net.perfectdreams.loritta.deviousfun.events.guild.voice

import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildVoiceMoveEvent(deviousShard: DeviousShard, gateway: DeviousGateway) : Event(deviousShard, gateway) {
    val member: Member
        get() = TODO()
    val channelJoined: Channel
        get() = TODO()
    val channelLeft: Channel
        get() = TODO()
}