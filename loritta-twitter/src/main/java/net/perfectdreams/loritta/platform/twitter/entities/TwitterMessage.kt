package net.perfectdreams.loritta.platform.twitter.entities

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import twitter4j.Status

class TwitterMessage(val message: Status) : Message {
    override val author: User
        get() = TwitterUser(message.user)
    override val content: String
        get() = message.text
    override val mentionedUsers: List<User>
        get() = listOf()
    override val channel = TwitterMessageChannel(message)

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}