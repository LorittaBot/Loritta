package com.mrpowergamerbr.loritta.utils

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.giveaway.payments.PaymentReason
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class LorittaLandRoleSync : Runnable {
	companion object {
		val logger = LoggerFactory.getLogger(LorittaLandRoleSync::class.java)
	}

	override fun run() {
		try {
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

			logger.info("Dando cargos especiais da LorittaLand...")

			// ===[ DESENHISTAS ]===
			val drawingRole = originalGuild.getRoleById("341343754336337921")

			logger.info("Processando cargos de desenhistas...")
			val validIllustrators = loritta.fanArts.mapNotNull {
				val artist = loritta.fanArtConfig.artists[it.artistId]
				val discordId = artist?.discordId ?: it.artistId
				if (discordId != null) {
					originalGuild.getMemberById(discordId)
				} else {
					null
				}
			}

			for (illustrator in validIllustrators) {
				if (!illustrator.roles.contains(drawingRole)) {
					logger.info("Dando o cargo de desenhista para ${illustrator.user.id}...")
					originalGuild.controller.addSingleRoleToMember(illustrator, drawingRole).queue()
				}
			}

			val invalidIllustrators = originalGuild.getMembersWithRoles(drawingRole).filter { !validIllustrators.contains(it) }
			invalidIllustrators.forEach {
				logger.info("Removendo cargo de desenhista de ${it.user.id}...")
				originalGuild.controller.removeSingleRoleFromMember(it, drawingRole).queue()
			}

			// ===[ PARCEIROS ]===
			logger.info("Processando cargos de parceiros...")
			if (!loritta.lorittaShards.shardManager.shards.any { it.status != JDA.Status.CONNECTED }) {
				val partnerRole = originalGuild.getRoleById("434512654292221952")
				val partnerServerConfigs = loritta.serversColl.find(
						Filters.eq(
								"serverListConfig.partner",
								true
						)
				)

				val validPartners = mutableListOf<Member>()

				val partnerGuilds = partnerServerConfigs.mapNotNull { lorittaShards.getGuildById(it.guildId) }
				partnerGuilds.forEach {
					val partners = it.members.filter { it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER) }
					for (partner in partners) {
						val member = originalGuild.getMember(partner.user) ?: continue
						validPartners.add(member)
						if (!member.roles.contains(partnerRole)) {
							logger.info("Dando o cargo de parceiro para ${member.user.id}...")
							originalGuild.controller.addSingleRoleToMember(member, partnerRole).queue()
						}
					}
				}

				val invalidPartners = originalGuild.getMembersWithRoles(partnerRole).filter { !validPartners.contains(it) }
				invalidPartners.forEach {
					logger.info("Removendo cargo de parceiro de ${it.user.id}...")
					originalGuild.controller.removeSingleRoleFromMember(it, partnerRole).queue()
				}
			} else {
				logger.warn("Todas as shards não estão carregadas! Ignorando cargos de parceiros...")
			}

			logger.info("Sincronizando cargos da LorittaLand...")

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
			val payments = transaction(Databases.loritta) {
				Payment.find {
					(Payments.reason eq PaymentReason.DONATION)
				}.toMutableList()
			}

			val donatorsPlusQuantity = mutableMapOf<Long, Double>()
			val inactiveDonators = mutableSetOf<Long>()

			for (payment in payments) {
				if (System.currentTimeMillis() >= payment.expiresAt ?: 0) {
					donatorsPlusQuantity[payment.userId] = payment.money.toDouble() + donatorsPlusQuantity.getOrDefault(payment.userId, 0.0)
				} else {
					inactiveDonators.add(payment.userId)
				}
			}

			val donatorRole = originalGuild.getRoleById("364201981016801281")
			val superDonatorRole = originalGuild.getRoleById("463652112656629760")
			val megaDonatorRole = originalGuild.getRoleById("534659343656681474")
			val inactiveRole = originalGuild.getRoleById("435856512787677214")

			for (member in originalGuild.members) {
				val roles = member.roles.toMutableList()

				if (donatorsPlusQuantity.containsKey(member.user.idLong)) {
					val donated = donatorsPlusQuantity[member.user.idLong] ?: 0.0

					if (!roles.contains(donatorRole))
						roles.add(donatorRole)

					if (roles.contains(inactiveRole))
						roles.remove(inactiveRole)

					if (donated >= 99.99) {
						if (!roles.contains(megaDonatorRole))
							roles.add(megaDonatorRole)
					} else {
						if (roles.contains(megaDonatorRole))
							roles.remove(megaDonatorRole)
					}

					if (donated >= 59.99) {
						if (!roles.contains(superDonatorRole))
							roles.add(superDonatorRole)
					} else {
						if (roles.contains(superDonatorRole))
							roles.remove(superDonatorRole)
					}
				} else {
					if (roles.contains(donatorRole))
						roles.remove(donatorRole)

					if (roles.contains(superDonatorRole))
						roles.remove(superDonatorRole)

					if (roles.contains(megaDonatorRole))
						roles.remove(megaDonatorRole)

					if (!roles.contains(inactiveRole) && inactiveDonators.contains(member.user.idLong))
						roles.add(inactiveRole)
				}

				if (!(roles.containsAll(member.roles) && member.roles.containsAll(roles))) {// Novos cargos foram adicionados
					logger.info("Alterando cargos de ${member}, novos cargos serão $roles")
					member.guild.controller.modifyMemberRoles(member, roles).queue()
				}
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