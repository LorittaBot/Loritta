package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class LorittaLandRoleSync : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			val originalGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID) ?: run {
				logger.error("Erro ao sincronizar cargos! Servidor da Loritta (Original) não existe!")
				return
			}

			logger.info("Dando cargos especiais da LorittaLand...")

			// ===[ DESENHISTAS ]===
			val drawingRole = originalGuild.getRoleById("341343754336337921")

			logger.info("Processando cargos de desenhistas...")
			val validIllustrators = loritta.fanArtArtists.mapNotNull {
				val discordId = it.socialNetworks
						?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
						?.id

				if (discordId != null) {
					runBlocking { originalGuild.retrieveMemberOrNullById(discordId) }
				} else {
					null
				}
			}

			if (drawingRole != null) {
				for (illustrator in validIllustrators) {
					if (!illustrator.roles.contains(drawingRole)) {
						logger.info("Dando o cargo de desenhista para ${illustrator.user.id}...")
						originalGuild.addRoleToMember(illustrator, drawingRole).queue()
					}
				}

				val invalidIllustrators = originalGuild.getMembersWithRoles(drawingRole).filter { !validIllustrators.contains(it) }
				invalidIllustrators.forEach {
					logger.info("Removendo cargo de desenhista de ${it.user.id}...")
					originalGuild.removeRoleFromMember(it, drawingRole).queue()
				}
			}

			// ===[ TRADUTORES ]===
			synchronizeTranslatorsRoles(originalGuild)
		} catch (e: Exception) {
			logger.error("Erro ao sincronizar cargos!", e)
		}
	}

	fun synchronizeTranslatorsRoles(originalGuild: Guild) {
		val translatorRole = originalGuild.getRoleById("385579854336360449")

		logger.info("Processing translators roles...")
		val translators = loritta.localeManager.locales.flatMap { it.value.getList("loritta.translationAuthors") }.distinct()

		val validTranslators = translators.mapNotNull {
			runBlocking { originalGuild.retrieveMemberOrNullById(it) }
		}

		if (translatorRole != null) {
			for (translator in validTranslators) {
				if (!translator.roles.contains(translatorRole)) {
					logger.info("Giving translator role to ${translator.user.id}...")
					originalGuild.addRoleToMember(translator, translatorRole).queue()
				}
			}

			val invalidTranslators = originalGuild.getMembersWithRoles(translatorRole).filter { !validTranslators.contains(it) }
			invalidTranslators.forEach {
				logger.info("Removing translator role from ${it.user.id}...")
				originalGuild.removeRoleFromMember(it, translatorRole).queue()
			}
		}
	}
}