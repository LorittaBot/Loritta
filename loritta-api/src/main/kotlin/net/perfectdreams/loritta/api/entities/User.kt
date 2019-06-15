package net.perfectdreams.loritta.api.entities

interface User : Mentionable, Identifiable {
	val name: String
	val avatar: String?
	val avatarUrl: String?
	val isBot: Boolean
}