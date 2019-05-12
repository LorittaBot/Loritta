package net.perfectdreams.loritta.platform.amino.entities

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User

class AminoMessage(override val content: String) : Message {
    override val author: User
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mentionedUsers: List<User>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    init {
        println(content)
    }

    override suspend fun delete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}