package net.perfectdreams.loritta.morenitta.api.entities

import net.perfectdreams.loritta.common.entities.Identifiable
import net.perfectdreams.loritta.common.entities.Mentionable

interface User : Mentionable, Identifiable {
	val name: String
	val avatar: String?
	val avatarUrl: String?
	val isBot: Boolean
}