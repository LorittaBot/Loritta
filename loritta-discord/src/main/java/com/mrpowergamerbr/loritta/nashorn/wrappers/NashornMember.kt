package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.LorittaNashornException
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import java.awt.Color

/**
 * Wrapper de um membro de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
open class NashornMember(internal val member: Member) : NashornUser(member.user) {
	var requesterLimiter = 0

	@NashornCommand.NashornDocs()
	fun getNickname(): String {
		return member.effectiveName
	}

	@NashornCommand.NashornDocs(arguments = "role")
	fun addRole(role: NashornRole) {
		if (requesterLimiter >= 3)
			throw LorittaNashornException("Mais de três edições em um único comando!")

		requesterLimiter++
		member.guild.addRoleToMember(member, role.role).queue()
	}

	@NashornCommand.NashornDocs(arguments = "role")
	fun removeRole(role: NashornRole) {
		if (requesterLimiter >= 3)
			throw LorittaNashornException("Mais de três edições em um único comando!")

		requesterLimiter++
		member.guild.removeRoleFromMember(member, role.role).queue()
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
		return member.color!!
	}

	@NashornCommand.NashornDocs()
	fun inVoiceChannel(): Boolean {
		return member.voiceState!!.inVoiceChannel()
	}

	@NashornCommand.NashornDocs()
	fun isDeafened(): Boolean {
		return member.voiceState!!.isDeafened
	}

	@NashornCommand.NashornDocs()
	fun isGuildDeafened(): Boolean {
		return member.voiceState!!.isGuildDeafened
	}

	@NashornCommand.NashornDocs()
	fun isMuted(): Boolean {
		return member.voiceState!!.isMuted
	}

	@NashornCommand.NashornDocs()
	fun isGuildMuted(): Boolean {
		return member.voiceState!!.isGuildMuted
	}

	@NashornCommand.NashornDocs()
	fun isSelfMuted(): Boolean {
		return member.voiceState!!.isSelfMuted
	}

	@NashornCommand.NashornDocs()
	fun isSelfDeafened(): Boolean {
		return member.voiceState!!.isSelfDeafened
	}

	@NashornCommand.NashornDocs()
	fun getOnlineStatus(): OnlineStatus {
		return member.onlineStatus
	}

	@NashornCommand.NashornDocs()
	fun isOwner(): Boolean {
		return member.isOwner
	}
}