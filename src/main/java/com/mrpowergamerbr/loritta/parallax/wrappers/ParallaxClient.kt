package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.JDA

class ParallaxClient(private val jda: JDA) {
	val user: ParallaxUser = ParallaxUser(jda.selfUser)

	override fun equals(other: Any?): Boolean {
		return jda == other
	}
}