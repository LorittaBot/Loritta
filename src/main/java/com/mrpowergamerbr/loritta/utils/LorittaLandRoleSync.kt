package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import net.dv8tion.jda.core.entities.Guild
import org.jetbrains.exposed.sql.transactions.transaction
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
					"334734175531696128" to "420710241693466627"  // Notificar Novidades
			)

			val originalGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID) ?: run {
				logger.error("Erro ao sincronizar cargos! Servidor da Loritta (Original) não existe!")
				return
			}
			val usGuild = lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID) ?: run {
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
					manager.setHoisted(originalRole.isHoisted)
					changed = true
				}

				if (originalRole.isMentionable != usRole.isMentionable) {
					manager.setMentionable(originalRole.isMentionable)
					changed = true
				}

				if (changed) {
					logger.info("Atualizando ${usRole.name}...")
					manager.queue()
				}
			}

			// Give roles
			synchronizeRoles(originalGuild, usGuild, "351473717194522647", "421325022951637015") // Guarda-Costas
			synchronizeRoles(originalGuild, usGuild, "399301696892829706", "421325387889377291") // Suporte
			synchronizeRoles(originalGuild, usGuild, "341343754336337921", "467750037812936704") // Desenhistas
			synchronizeRoles(originalGuild, usGuild, "385579854336360449", "467750852610752561") // Tradutores
			synchronizeRoles(originalGuild, usGuild, "434512654292221952", "467751141363548171") // Lori Partner

			// Apply donators roles
			val donatorsProfiles = transaction(Databases.loritta) {
				Profile.find { Profiles.isDonator eq true }.toMutableList()
			}

			val donators = donatorsProfiles.mapNotNull {
				val member = originalGuild.getMemberById(it.userId)
				if (member != null)
					Pair(it, member)
				else
					null
			}

			for ((profile, member) in donators) {
				val isDonationStillValid = profile.isDonator && profile.donationExpiresIn > System.currentTimeMillis()
				val donatorRole = originalGuild.getRoleById("364201981016801281")
				val inactiveRole = originalGuild.getRoleById("435856512787677214")
				val roles = member.roles.toMutableList()

				if (isDonationStillValid) {
					if (!roles.contains(donatorRole))
						roles.add(donatorRole)

					if (roles.contains(inactiveRole))
						roles.remove(inactiveRole)
				} else {
					if (roles.contains(donatorRole))
						roles.remove(donatorRole)

					if (!roles.contains(inactiveRole))
						roles.add(inactiveRole)
				}

				if (!(roles.containsAll(member.roles) && member.roles.containsAll(roles))) // Novos cargos foram adicionados
					member.guild.controller.modifyMemberRoles(member, roles).queue()
			}
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
				toGuild.controller.removeSingleRoleFromMember(member, giveRole).queue()
			}
		}

		for (member in membersWithOriginalRole) {
			if (!membersWithNewRole.any { it.user.id == member.user.id }) {
				val usMember = toGuild.getMember(member.user) ?: continue

				logger.info("Adicionado cargo ${giveRole.id} para ${usMember.effectiveName} (${usMember.user.id})...")
				toGuild.controller.addSingleRoleToMember(usMember, giveRole).queue()
			}
		}
	}
}