package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.entities.User

class InteraKTionsMessage(private val message: net.perfectdreams.discordinteraktions.api.entities.Message) : Message {
    override val author: User
        get() = TODO("Not yet implemented")
    override val content: String
        get() = message.content
    override val mentionedUsers: List<User>
        get() = TODO("Not yet implemented")
    override val channel: MessageChannel
        get() = TODO("Not yet implemented")

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}