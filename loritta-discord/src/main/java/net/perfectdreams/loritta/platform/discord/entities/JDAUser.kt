package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.common.entities.User

class JDAUser(private val user: net.dv8tion.jda.api.entities.User) : User {
    override val id by user::idLong
    override val name by user::name
    override val asMention by user::asMention
    override val avatar
        get() = TODO()
}