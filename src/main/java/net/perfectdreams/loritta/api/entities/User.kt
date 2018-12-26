package net.perfectdreams.loritta.api.entities

interface User : Mentionable, Unique {
	val name: String
	val effectiveAvatarUrl: String
	val avatarUrl: String?
	val isBot: Boolean
}