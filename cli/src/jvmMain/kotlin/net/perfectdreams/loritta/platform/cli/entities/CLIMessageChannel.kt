package net.perfectdreams.loritta.platform.cli.entities

import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class CLIMessageChannel : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        println(message.content)
    }
}