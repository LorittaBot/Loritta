package net.perfectdreams.loritta.cinnamon.platform.cli.entities

import net.perfectdreams.loritta.cinnamon.common.entities.LorittaEmbed
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaMessage
import net.perfectdreams.loritta.cinnamon.common.entities.MessageChannel
import java.io.File

class CLIMessageChannel : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        println(message.content)

        for (reply in message.replies) {
            println(reply.prefix + " | " + reply.content)
        }

        message.embed?.let { println(translateEmbed(it)) }

        message.files.entries.forEach {
            File(it.key).writeBytes(it.value)
            println("Saved file ${it.key}")
        }
    }

    private fun translateEmbed(embed: LorittaEmbed): String = buildString {
        append("Title: ${embed.title}\n")
        append("Description: ${embed.description}\n")

        embed.fields.forEach {
            append("${it.name}: ${it.value}\n")
        }
    }
}