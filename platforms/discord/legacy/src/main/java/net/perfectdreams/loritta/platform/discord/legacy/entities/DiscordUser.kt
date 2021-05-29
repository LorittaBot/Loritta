package net.perfectdreams.loritta.platform.discord.legacy.entities

import net.perfectdreams.loritta.api.entities.User

interface DiscordUser : User {
	val discriminator: String
	val effectiveAvatarUrl: String
	val defaultAvatarUrl: String
}