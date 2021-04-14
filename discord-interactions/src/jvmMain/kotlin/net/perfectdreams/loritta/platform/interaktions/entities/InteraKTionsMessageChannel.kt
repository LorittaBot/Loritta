package net.perfectdreams.loritta.platform.interaktions.entities

import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class InteraKTionsMessageChannel(val context: SlashCommandContext) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        context.sendMessage {
            content = message.content
        }
    }
}