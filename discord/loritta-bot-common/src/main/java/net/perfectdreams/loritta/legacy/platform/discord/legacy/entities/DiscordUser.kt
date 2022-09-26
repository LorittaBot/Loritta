package net.perfectdreams.loritta.legacy.platform.discord.legacy.entities

import net.perfectdreams.loritta.legacy.api.entities.User

interface DiscordUser : User {
	val discriminator: String
	val effectiveAvatarUrl: String
	val defaultAvatarUrl: String
}