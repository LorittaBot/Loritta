package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import java.awt.Color

/**
 * Wrapper de um membro de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
open class NashornMember(internal val member: Member) : NashornUser(member.user) {
	fun getNickname(): String {
		return member.effectiveName
	}

	fun addRole(role: NashornRole) {
		member.guild.controller.addRolesToMember(member, role.role).complete()
	}

	fun removeRole(role: NashornRole) {
		member.guild.controller.removeRolesFromMember(member, role.role).complete()
	}

	fun getRoles(): MutableList<NashornRole> {
		val roles = mutableListOf<NashornRole>()

		member.roles.forEach {
			roles.add(NashornRole(it))
		}

		return roles
	}

	fun hasRole(role: NashornRole) : Boolean {
		return member.roles.contains(role.role)
	}

	fun getColor(): Color {
		return member.color
	}

	fun getAsMention(): String {
		return member.asMention
	}

	fun inVoiceChannel(): Boolean {
		return member.voiceState.inVoiceChannel()
	}

	fun isDeafened(): Boolean {
		return member.voiceState.isDeafened
	}

	fun isGuildDeafened(): Boolean {
		return member.voiceState.isGuildDeafened
	}

	fun isMuted(): Boolean {
		return member.voiceState.isMuted
	}

	fun isGuildMuted(): Boolean {
		return member.voiceState.isGuildMuted
	}

	fun isSelfMuted(): Boolean {
		return member.voiceState.isSelfMuted
	}

	fun isSelfDeafened(): Boolean {
		return member.voiceState.isSelfDeafened
	}

	fun isPlaying(): Boolean {
		return member.game != null
	}

	fun isStreaming(): Boolean {
		return member.game.type == Game.GameType.TWITCH
	}

	fun getGameName(): String {
		return member.game.name
	}

	fun getGameUrl(): String {
		return member.game.url
	}

	fun getOnlineStatus(): OnlineStatus {
		return member.onlineStatus
	}

	fun isOwner(): Boolean {
		return member.isOwner
	}
}