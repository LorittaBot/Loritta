package net.perfectdreams.loritta.cinnamon.platform.cli.entities

import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.entities.UserAvatar

class CLIUser : User {
    override val id: Long = 0L
    override val name: String
        get() = TODO("Not yet implemented")
    override val avatar: UserAvatar
        get() = TODO("Not yet implemented")
    override val asMention: String
        get() = TODO("Not yet implemented")
}