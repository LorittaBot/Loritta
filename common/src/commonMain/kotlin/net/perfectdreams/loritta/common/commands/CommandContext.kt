package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.locale.BaseLocale

class CommandContext(
    val locale: BaseLocale,
    val channel: MessageChannel
) {
    suspend fun sendMessage(message: String) {
        channel.sendMessage(message)
    }
}