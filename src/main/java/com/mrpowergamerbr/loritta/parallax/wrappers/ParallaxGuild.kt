package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.entities.Guild

class ParallaxGuild(private val guild: Guild) {
	// TODO: afkChannel
	val afkChannelID get() = guild.afkChannel.id

	fun getRolesByName(name: String): List<ParallaxRole> {
		return getRolesByName(name, false)
	}

	fun getRolesByName(name: String, ignoreCase: Boolean = false): List<ParallaxRole> {
		return guild.getRolesByName(name, ignoreCase).map { ParallaxRole(it) }
	}

	fun getTextChannelsByName(name: String): List<ParallaxTextChannel> {
		return getTextChannelsByName(name, false)
	}

	fun getTextChannelsByName(name: String, ignoreCase: Boolean = false): List<ParallaxTextChannel> {
		return guild.getTextChannelsByName(name, ignoreCase).map { ParallaxTextChannel(it) }
	}
}