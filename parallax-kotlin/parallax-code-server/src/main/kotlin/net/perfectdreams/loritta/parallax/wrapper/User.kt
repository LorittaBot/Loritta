package net.perfectdreams.loritta.parallax.wrapper

class User(
		val id: Long,
		val username: String,
		val discriminator: String,
		val avatar: String
) {
	override fun toString() = "<@$id>"
}