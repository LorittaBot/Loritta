package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed

class ParallaxEmbed {
	fun toDiscordEmbed(): MessageEmbed {
		return EmbedBuilder().build()
	}
}