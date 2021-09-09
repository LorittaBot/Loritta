package net.perfectdreams.loritta.cinnamon.discord.entities

import net.perfectdreams.loritta.cinnamon.common.entities.User

interface DiscordUser : User {
    val banner: String?
}