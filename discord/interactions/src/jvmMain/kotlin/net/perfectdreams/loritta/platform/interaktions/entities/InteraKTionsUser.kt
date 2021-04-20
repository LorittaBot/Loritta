package net.perfectdreams.loritta.platform.interaktions.entities

import net.perfectdreams.loritta.common.entities.User

class InteraKTionsUser(val user: net.perfectdreams.discordinteraktions.api.entities.User) : User {
    override val id: Long
        get() = user.id.value
    override val name by user::username
    override val avatarUrl
        get() = TODO()
    override val asMention: String
        get() = "<@${id}>"
}