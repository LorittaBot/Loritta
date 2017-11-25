package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import net.dv8tion.jda.core.entities.Role
import java.awt.Color

class NashornRole(internal val role: Role) {
	@NashornCommand.NashornDocs(
			"Retorna o nome da role",
			"",
			"wow"
	)
	fun getName(): String {
		return role.name
	}

	@NashornCommand.NashornDocs(
			"Retorna o ID da role",
			"",
			"wow"
	)
	fun getId(): String {
		return role.id
	}

	@NashornCommand.NashornDocs("Retorna a versão \"mencionável\" de uma role",
			"",
			"wow")
	fun getAsMention(): String {
		return role.asMention
	}

	@NashornCommand.NashornDocs("Retorna a cor da role",
			"",
			"wow")
	fun getColor(): Color {
		return role.color
	}

	@NashornCommand.NashornDocs("Retorna se a role aparece separadamente na lista de usuários online",
			"",
			"wow")
	fun isHoisted(): Boolean {
		return role.isHoisted
	}

	@NashornCommand.NashornDocs("Retorna se a role foi criada pela inclusão de um bot",
			"",
			"wow")
	fun isManaged(): Boolean {
		return role.isManaged
	}

	@NashornCommand.NashornDocs("Retorna se a role é pública (ou seja, @everyone)",
			"",
			"wow")
	fun isPublicRole(): Boolean {
		return role.isPublicRole
	}

	@NashornCommand.NashornDocs("Retorna se a role é mencionável",
			"",
			"wow")
	fun isMentionable(): Boolean {
		return role.isMentionable
	}

	@NashornCommand.NashornDocs("Exclui a role",
			"",
			"wow")
	fun delete() {
		role.delete().complete()
	}
}