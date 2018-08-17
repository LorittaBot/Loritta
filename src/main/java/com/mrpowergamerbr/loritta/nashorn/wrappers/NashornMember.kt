package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import java.awt.Color

/**
 * Wrapper de um membro de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
open class NashornMember(internal val member: Member) : NashornUser(member.user) {
	@NashornCommand.NashornDocs()
	fun getNickname(): String {
		return member.effectiveName
	}

	@NashornCommand.NashornDocs(arguments = "role")
	fun addRole(role: NashornRole) {
		member.guild.controller.addRolesToMember(member, role.role).complete()
	}

	@NashornCommand.NashornDocs(arguments = "role")
	fun removeRole(role: NashornRole) {
		member.guild.controller.removeRolesFromMember(member, role.role).complete()
	}

	@NashornCommand.NashornDocs()
	fun getRoles(): MutableList<NashornRole> {
		val roles = mutableListOf<NashornRole>()

		member.roles.forEach {
			roles.add(NashornRole(it))
		}

		return roles
	}

	@NashornCommand.NashornDocs(arguments = "role")
	fun hasRole(role: NashornRole) : Boolean {
		return member.roles.contains(role.role)
	}

	@NashornCommand.NashornDocs()
	fun getColor(): Color {
		return member.color
	}

	@NashornCommand.NashornDocs()
	fun inVoiceChannel(): Boolean {
		return member.voiceState.inVoiceChannel()
	}

	@NashornCommand.NashornDocs()
	fun isDeafened(): Boolean {
		return member.voiceState.isDeafened
	}

	@NashornCommand.NashornDocs()
	fun isGuildDeafened(): Boolean {
		return member.voiceState.isGuildDeafened
	}

	@NashornCommand.NashornDocs()
	fun isMuted(): Boolean {
		return member.voiceState.isMuted
	}

	@NashornCommand.NashornDocs()
	fun isGuildMuted(): Boolean {
		return member.voiceState.isGuildMuted
	}

	@NashornCommand.NashornDocs()
	fun isSelfMuted(): Boolean {
		return member.voiceState.isSelfMuted
	}

	@NashornCommand.NashornDocs()
	fun isSelfDeafened(): Boolean {
		return member.voiceState.isSelfDeafened
	}

	@NashornCommand.NashornDocs()
	fun isPlaying(): Boolean {
		return member.game != null
	}

	@NashornCommand.NashornDocs()
	fun isStreaming(): Boolean {
		return member.game.type == Game.GameType.STREAMING
	}

	@NashornCommand.NashornDocs()
	fun getGameName(): String {
		return member.game.name
	}

	@NashornCommand.NashornDocs()
	fun getGameUrl(): String {
		return member.game.url
	}

	@NashornCommand.NashornDocs()
	fun getOnlineStatus(): OnlineStatus {
		return member.onlineStatus
	}

	@NashornCommand.NashornDocs()
	fun isOwner(): Boolean {
		return member.isOwner
	}

	@NashornCommand.NashornDocs("Altera o nome de um usuário",
			"novoNickname")
	fun setNickname(nickname: String) {
		if (nickname.contains(Loritta.config.clientToken, true)) {
			NashornContext.securityViolation(null)
			return null!!
		}

		member.guild.controller.setNickname(member, nickname).complete()
	}
}