package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.common.entities.User

class JDAUser(internal val user: net.dv8tion.jda.api.entities.User) : User {
    override val id: Long
        get() = user.idLong
    override val name: String
        get() = user.name
    override val asMention: String
        get() = user.asMention
    override val avatar
        get() = TODO()
}