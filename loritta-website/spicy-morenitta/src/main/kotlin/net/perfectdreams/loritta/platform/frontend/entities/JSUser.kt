package net.perfectdreams.loritta.platform.frontend.entities

import net.perfectdreams.loritta.api.entities.User

class JSUser(override val name: String) : User {
	override val avatar: String?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val avatarUrl: String?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val isBot: Boolean
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val asMention: String
		get() = name
	override val id: Long
		get() = name.hashCode().toLong()
}