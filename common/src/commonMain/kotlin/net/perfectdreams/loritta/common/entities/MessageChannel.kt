package net.perfectdreams.loritta.common.entities

import net.perfectdreams.loritta.common.builder.MessageBuilder

interface MessageChannel {
    suspend fun sendMessage(content: String) = sendMessage {
        this.content = content
    }

    suspend fun sendMessage(block: net.perfectdreams.loritta.common.builder.MessageBuilder.() -> (Unit)) = sendMessage(
        net.perfectdreams.loritta.common.builder.MessageBuilder().apply(block).build())
    suspend fun sendMessage(message: LorittaMessage)
}