package net.perfectdreams.loritta.platform.interaktions.entities

import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class InteraKTionsMessageChannel(val context: SlashCommandContext) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        context.sendMessage {
            content = buildString {
                append(message.content)

                val embed = message.embed
                if (embed != null) {
                    append("\n\n")
                    append(translateEmbed(embed))
                }
            }

            for (file in message.files) {
                addFile(file.key, file.value.inputStream())
            }

            // Keep in mind that ephemeral messages do not support *everything*, so let's throw a exception if
            // we are using stuff that Discord ephemeral messages do not support
            if (message.isEphemeral) {
                if (message.embed != null)
                    throw UnsupportedOperationException("Ephemeral Messages do not support embeds!")
                if (message.files.isNotEmpty())
                    throw UnsupportedOperationException("Ephemeral Messages do not support files!")

                flags = MessageFlags(MessageFlag.Ephemeral)
            }
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