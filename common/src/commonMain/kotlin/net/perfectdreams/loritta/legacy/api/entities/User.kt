package net.perfectdreams.loritta.legacy.api.entities

interface User : Mentionable, Identifiable {
	val name: String
	val avatar: String?
	val avatarUrl: String?
	val isBot: Boolean
}