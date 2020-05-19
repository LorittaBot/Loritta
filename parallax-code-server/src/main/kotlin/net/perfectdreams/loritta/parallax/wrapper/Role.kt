package net.perfectdreams.loritta.parallax.wrapper

class Role(
		val id: Long,
		val name: String
) {
	lateinit var guild: Guild

	override fun toString() = "<@&$id>"
}