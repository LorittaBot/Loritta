package net.perfectdreams.loritta.deviousfun.events.guild.voice

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildVoiceLeaveEvent(deviousFun: DeviousFun, gateway: DeviousGateway) : Event(deviousFun, gateway) {
    val member: Member
        get() = TODO()
    val channelLeft: Channel
        get() = TODO()
}