package net.perfectdreams.loritta.platform.twitter.entities

import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.entities.UserAvatar

class TwitterUser : User {
    override val id: Long = 0L
    override val name: String
        get() = TODO("Not yet implemented")
    override val avatar: UserAvatar
        get() = TODO("Not yet implemented")
    override val asMention: String
        get() = TODO("Not yet implemented")
}