package net.perfectdreams.loritta.legacy.common.entities

import net.perfectdreams.loritta.legacy.common.builder.MessageBuilder

interface MessageChannel {
    suspend fun sendMessage(content: String) = sendMessage {
        this.content = content
    }

    suspend fun sendMessage(block: MessageBuilder.() -> (Unit)) = sendMessage(MessageBuilder().apply(block).build())
    suspend fun sendMessage(message: LorittaMessage)
}