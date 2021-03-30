package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User

class DummyMessage(override val channel: InteraKTionsChannel) : Message {
    override val author: User
        get() = TODO("Not yet implemented")
    override val content: String
        get() = TODO("Not yet implemented")
    override val mentionedUsers: List<User>
        get() = TODO("Not yet implemented")

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}