package net.perfectdreams.loritta.platform.kord.entities

import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class KordMessageChannel(private val handle: dev.kord.core.entity.channel.MessageChannel) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        handle.createMessage(message.content)
    }
}