package net.perfectdreams.loritta.deviousfun.events.guild.voice

import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildVoiceJoinEvent(jda: JDA, gateway: DeviousGateway) : Event(jda, gateway) {
    val member: Member
        get() = TODO()
    val channelJoined: Channel
        get() = TODO()
}