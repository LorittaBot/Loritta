package net.perfectdreams.loritta.discord.entities

import net.perfectdreams.loritta.common.entities.User

interface DiscordUser : User {
    val banner: String?
}