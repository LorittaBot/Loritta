package net.perfectdreams.loritta.morenitta.api.entities

import net.perfectdreams.loritta.common.entities.Channel
import net.perfectdreams.loritta.morenitta.messages.LorittaMessage

interface MessageChannel : Channel {
    suspend fun sendMessage(content: String) = sendMessage(LorittaMessage(content))
    suspend fun sendMessage(message: LorittaMessage): Message
    suspend fun sendFile(bytes: ByteArray, fileName: String, content: String) =
        sendFile(bytes, fileName, LorittaMessage(content))

    suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message
}