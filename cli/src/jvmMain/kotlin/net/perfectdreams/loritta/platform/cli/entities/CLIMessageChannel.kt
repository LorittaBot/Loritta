package net.perfectdreams.loritta.platform.cli.entities

import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class CLIMessageChannel : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        println(message.content)
        println(translateEmbed(message.embed ?: return))
    }

    private fun translateEmbed(embed: LorittaEmbed): String = buildString {
        append("Title: ${embed.title}\n")
        append("Description: ${embed.description}\n")

        embed.fields.forEach {
            append("${it.name}: ${it.value}\n")
        }
    }
}