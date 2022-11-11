package net.perfectdreams.loritta.deviousfun.events.message.react

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.MessageReaction
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.events.message.GenericMessageEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

open class GenericMessageReactionEvent(
    deviousShard: DeviousShard,
    gateway: DeviousGateway,
    val user: User,
    messageIdSnowflake: Snowflake,
    channel: Channel,
    val reaction: MessageReaction
) : GenericMessageEvent(deviousShard, gateway, messageIdSnowflake, channel) {
    val reactionEmote = reaction.reactionEmote
}