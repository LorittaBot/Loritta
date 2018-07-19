package com.mrpowergamerbr.loritta.utils

import net.dv8tion.jda.core.entities.Guild
import org.slf4j.LoggerFactory

class LorittaLandRoleSync : Runnable {
	companion object {
		val logger = LoggerFactory.getLogger(LorittaLandRoleSync::class.java)
	}

	override fun run() {
		try {
			logger.info("Sincronizando cargos da LorittaLand...")

			val roleRemap = mutableMapOf(
					"316363779518627842" to "420630427837923328", // Deusas Supremas
					"301764115582681088" to "420630186061725696", // Loritta (Integration)
					"351473717194522647" to "421325022951637015", // Guarda-Costas da Lori
					"399301696892829706" to "421325387889377291", // Suporte
					"341343754336337921" to "467750037812936704", // Desenhistas
					"385579854336360449" to "467750852610752561", // Tradutores
					"434512654292221952" to "467751141363548171", // Loritta Partner
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
					manager.setPermissions(originalRole.permissionsRaw)
					changed = true
				}

				if (originalRole.isHoisted != usRole.isHoisted) {
					manager.setHoisted(true)
					changed = true
				}

				if (originalRole.isMentionable != usRole.isMentionable) {
					manager.setMentionable(true)
					changed = true
				}

				if (changed) {
					logger.info("Atualizando ${usRole.name}...")
					manager.complete()
				}
			}

			// Give roles
			synchronizeRoles(originalGuild, usGuild, "351473717194522647", "421325022951637015") // Guarda-Costas
			synchronizeRoles(originalGuild, usGuild, "399301696892829706", "421325387889377291") // Suporte
			synchronizeRoles(originalGuild, usGuild, "341343754336337921", "467750037812936704") // Desenhistas
			synchronizeRoles(originalGuild, usGuild, "385579854336360449", "467750852610752561") // Tradutores
			synchronizeRoles(originalGuild, usGuild, "434512654292221952", "467751141363548171") // Lori Partner
		} catch (e: Exception) {
			logger.error("Erro ao sincronizar cargos!", e)
		}
	}

	fun synchronizeRoles(fromGuild: Guild, toGuild: Guild, originalRoleId: String, giveRoleId: String) {
		val originalRole = fromGuild.getRoleById(originalRoleId)
		val giveRole = toGuild.getRoleById(giveRoleId)

		val membersWithOriginalRole = fromGuild.getMembersWithRoles(originalRole)
		val membersWithNewRole = toGuild.getMembersWithRoles(giveRole)

		for (member in membersWithNewRole) {
			if (!membersWithOriginalRole.any { it.user.id == member.user.id }) {
				logger.info("Removendo cargo  ${giveRole.id} de ${member.effectiveName} (${member.user.id})...")
				toGuild.controller.removeSingleRoleFromMember(member, giveRole).complete()
			}
		}

		for (member in membersWithOriginalRole) {
			if (!membersWithNewRole.any { it.user.id == member.user.id }) {
				val usMember = toGuild.getMember(member.user) ?: continue

				logger.info("Adicionado cargo ${giveRole.id} para ${usMember.effectiveName} (${usMember.user.id})...")
				toGuild.controller.addSingleRoleToMember(usMember, giveRole).complete()
			}
		}
	}
}