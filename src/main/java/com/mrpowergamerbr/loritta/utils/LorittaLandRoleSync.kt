package com.mrpowergamerbr.loritta.utils

import net.dv8tion.jda.core.entities.Guild
import org.slf4j.LoggerFactory

class LorittaLandRoleSync : Runnable {
	companion object {
		val logger = LoggerFactory.getLogger(LorittaLandRoleSync::class.java)
	}

	override fun run() {
		logger.info("Sincronizando cargos da LorittaLand...")

		val roleRemap = mutableMapOf(
				"316363779518627842" to "420630427837923328", // Deusas Supremas
				"301764115582681088" to "420630186061725696", // Loritta (Integration)
				"351473717194522647" to "421325022951637015", // Guarda-Costas da Lori
				"334734175531696128" to "420710241693466627" // Notificar Novidades
		)

		val originalGuild = lorittaShards.getGuildById("297732013006389252") ?: run {
			logger.error("Erro ao sincronizar cargos! Servidor da Loritta (Original) não existe!")
			return
		}
		val usGuild = lorittaShards.getGuildById("420626099257475072") ?: run {
			logger.error("Erro ao sincronizar cargos! Servidor da Loritta (Inglês) não existe!")
			return
		}

		for ((originalRoleId, usRoleId) in roleRemap) {
			val originalRole = originalGuild.getRoleById(originalRoleId)
			val usRole = usGuild.getRoleById(usRoleId)

			val manager = usRole.manager
			var changed = false

			if (originalRole.color != usRole.color) {
				manager.setColor(originalRole.color)
				changed = true
			}

			if (originalRole.permissionsRaw != usRole.permissionsRaw) {
				manager.setPermissions(usRole.permissionsRaw)
				changed = true
			}

			if (changed) {
				logger.info("Atualizando ${usRole.name}...")
				manager.complete()
			}
		}

		// Give roles
		synchronizeRoles(originalGuild, usGuild, "351473717194522647", "421325022951637015")
	}

	fun synchronizeRoles(fromGuild: Guild, toGuild: Guild, originalRoleId: String, giveRoleId: String) {
		val originalRole = fromGuild.getRoleById(originalRoleId)
		val giveRole = fromGuild.getRoleById(giveRoleId)

		val membersWithOriginalRole = fromGuild.getMembersWithRoles(originalRole)
		val membersWithNewRole = toGuild.getMembersWithRoles(giveRole)

		for (member in membersWithNewRole) {
			if (!membersWithOriginalRole.any { it.user == member.user }) {
				logger.info("Removendo cargo  ${giveRole.id} de ${member.effectiveName} (${member.user.id})...")
				toGuild.controller.removeSingleRoleFromMember(member, giveRole).complete()
			}
		}

		for (member in membersWithOriginalRole) {
			if (!membersWithNewRole.any { it.user == member.user }) {
				logger.info("Adicionado cargo ${giveRole.id} para ${member.effectiveName} (${member.user.id})...")
				toGuild.controller.addSingleRoleToMember(member, giveRole).complete()
			}
		}
	}
}