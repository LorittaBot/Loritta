package net.perfectdreams.loritta.platform.nodejs.entities

import net.perfectdreams.loritta.api.entities.User

class ErisUser(val user: eris.User) : User {
	override val name: String
		get() = user.username
	override val avatar: String?
		get() = user.avatar
	override val avatarUrl: String?
		get() = user.avatarURL
	override val isBot: Boolean
		get() = user.bot
	override val asMention: String
		get() = user.mention
	override val id: Long
		get() = user.id.toLong()

}