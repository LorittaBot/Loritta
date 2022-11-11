package net.perfectdreams.loritta.deviousfun.events.message.react

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class MessageReactionRemoveEvent(
    deviousShard: DeviousShard,
    gateway: DeviousGateway,
    user: User,
    messageIdSnowflake: Snowflake,
    channel: Channel,
    messageReaction: MessageReaction,
    val guild: Guild?,
    val member: Member?
) : GenericMessageReactionEvent(deviousShard, gateway, user, messageIdSnowflake, channel, messageReaction)