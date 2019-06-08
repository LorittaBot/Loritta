package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild

class ParallaxGuild(private val guild: Guild) {
	val afkChannel get() = ParallaxVoiceChannel(guild.afkChannel!!)
	val afkChannelID get() = guild.afkChannel!!.id
	val afkTimeout get() = guild.afkTimeout.seconds

	val database = ParallaxDatabase(guild)

	val me get() = ParallaxMember(guild.selfMember)

	val iconURL get() = guild.iconUrl

	val owner = ParallaxMember(guild.owner!!)
	val ownerId = guild.ownerId

	val roles get() = guild.roles.map { ParallaxRole(it) }
	val members get() = guild.members.map { ParallaxMember(it) }

	fun getMemberById(memberId: String): ParallaxMember? {
		return members.firstOrNull { it.id == memberId }
	}

	fun getMemberById(memberId: Long): ParallaxMember? {
		return members.firstOrNull { it.id == memberId.toString() }
	}

	fun getMember(user: ParallaxUser): ParallaxMember? {
		return this.getMemberById(user.id)
	}

	@JvmOverloads
	fun getRolesByName(name: String, ignoreCase: Boolean = false): List<ParallaxRole> {
		return guild.getRolesByName(name, ignoreCase).map { ParallaxRole(it) }
	}

	fun getRoleById(roleId: String): ParallaxRole? {
		return roles.firstOrNull { it.id == roleId }
	}

	fun getRoleById(roleId: Long): ParallaxRole? {
		return roles.firstOrNull { it.id == roleId.toString() }
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