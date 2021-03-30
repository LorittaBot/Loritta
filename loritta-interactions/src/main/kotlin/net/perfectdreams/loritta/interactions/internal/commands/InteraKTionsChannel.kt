package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaMessage

class InteraKTionsChannel(private val context: SlashCommandContext) : MessageChannel {
    override val name: String
        get() = TODO("Not yet implemented")
    override val participants: List<Member>
        get() = TODO("Not yet implemented")

    override suspend fun sendMessage(content: String): Message {
        return super.sendMessage(content)
    }

    override suspend fun sendMessage(message: LorittaMessage): Message {
        return InteraKTionsMessage(
            context.sendMessage {
                this.content = message.content
            }
        )
    }

    override suspend fun sendFile(bytes: ByteArray, fileName: String, content: String): Message {
        return super.sendFile(bytes, fileName, content)
    }

    override suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message {
        TODO("Not yet implemented")
    }
}