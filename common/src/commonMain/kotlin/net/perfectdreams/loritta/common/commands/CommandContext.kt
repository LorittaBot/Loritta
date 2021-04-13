package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.entities.MessageChannel

class CommandContext(
    val channel: MessageChannel
) {
    suspend fun sendMessage(message: String) {
        channel.sendMessage(message)
    }
}