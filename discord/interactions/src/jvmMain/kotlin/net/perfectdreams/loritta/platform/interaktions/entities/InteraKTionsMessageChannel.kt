package net.perfectdreams.loritta.platform.interaktions.entities

import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.common.builder.AllowedMentionsBuilder
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class InteraKTionsMessageChannel(val context: SlashCommandContext) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        context.sendMessage {
            content = buildString {
                if (message.content != null)
                    append(message.content)

                for (reply in message.replies) {
                    append("\n")
                    append("${reply.prefix} **|** ${reply.content}")
                }

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

            // Allowed Mentions
            // By default we will disable ALL mentions, to avoid a "@everyone moment 3.0"
            // Messages can enable specific mentions if needed
            allowedMentions = dev.kord.rest.builder.message.AllowedMentionsBuilder().apply {
                for (user in message.allowedMentions.users) {
                    users.add(Snowflake(user.id))
                }

                repliedUser = message.allowedMentions.repliedUser
            }.build()
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