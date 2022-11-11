package net.perfectdreams.loritta.deviousfun.events.message.create

import dev.kord.gateway.MessageCreate
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.message.GenericMessageEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class MessageReceivedEvent(
    deviousShard: DeviousShard,
    gateway: DeviousGateway,
    val author: User,
    val message: Message,
    channel: Channel,
    val guild: Guild?,
    val member: Member?,
    val event: MessageCreate
) : GenericMessageEvent(deviousShard, gateway, event.message.id, channel)