package net.perfectdreams.loritta.platform.amino.entities

import net.perfectdreams.loritta.api.entities.User

class AminoUser(override val name: String) : User {
    override val id: Long
        get() = name.hashCode().toLong()
    override val avatar: String?
        get() = "https://cdn.discordapp.com/emojis/523176710439567392.png"
    override val avatarUrl: String?
        get() = "https://cdn.discordapp.com/emojis/523176710439567392.png"
    override val isBot: Boolean
        get() = false
    override val asMention: String
        get() = name
}