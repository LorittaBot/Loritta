package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.Guild

class ParallaxGuild(private val guild: Guild) {
	// TODO: afkChannel
	val afkChannelID get() = guild.afkChannel.id
	val database = ParallaxDatabase(guild)

	@JvmOverloads
	fun getRolesByName(name: String, ignoreCase: Boolean = false): List<ParallaxRole> {
		return guild.getRolesByName(name, ignoreCase).map { ParallaxRole(it) }
	}

	fun getRoleById(roleId: String): ParallaxRole? {
		return ParallaxRole(guild.getRoleById(roleId))
	}

	fun getRoleById(roleId: Long): ParallaxRole? {
		return ParallaxRole(guild.getRoleById(roleId))
	}

	fun getTextChannelsByName(name: String): List<ParallaxTextChannel> {
		return getTextChannelsByName(name, false)
	}

	fun getTextChannelsByName(name: String, ignoreCase: Boolean = false): List<ParallaxTextChannel> {
		return guild.getTextChannelsByName(name, ignoreCase).map { ParallaxTextChannel(it) }
	}

	@JvmOverloads
	fun ban(user: ParallaxUser, options: Map<String, Any> = mapOf()) {
		ban(user, ParallaxUser(guild.selfMember.user), options)
	}

	@JvmOverloads
	fun ban(user: ParallaxUser, punisher: ParallaxUser, options: Map<String, Any> = mapOf()) {
		val serverConfig = loritta.getServerConfigForGuild(guild.id)
		BanCommand.ban(
				loritta.getServerConfigForGuild(guild.id),
				guild,
				punisher.user,
				loritta.getLegacyLocaleById(serverConfig.localeId),
				user.user,
				options["reason"] as String? ?: "",
				options["isSilent"] as Boolean? ?: false,
				options["days"] as Int? ?: 0
		)
	}
}