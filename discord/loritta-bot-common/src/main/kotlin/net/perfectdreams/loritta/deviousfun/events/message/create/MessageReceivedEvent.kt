package net.perfectdreams.loritta.deviousfun.events.message.create

import dev.kord.gateway.MessageCreate
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.message.GenericMessageEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class MessageReceivedEvent(
    jda: JDA,
    gateway: DeviousGateway,
    val author: User,
    val message: Message,
    channel: Channel,
    val guild: Guild?,
    val member: Member?,
    val event: MessageCreate
) : GenericMessageEvent(jda, gateway, event.message.id, channel)