package net.perfectdreams.loritta.morenitta.entities

import net.perfectdreams.loritta.morenitta.builder.MessageBuilder

interface MessageChannel {
    suspend fun sendMessage(content: String) = sendMessage {
        this.content = content
    }

    suspend fun sendMessage(block: MessageBuilder.() -> (Unit)) = sendMessage(MessageBuilder().apply(block).build())
    suspend fun sendMessage(message: LorittaMessage)
}