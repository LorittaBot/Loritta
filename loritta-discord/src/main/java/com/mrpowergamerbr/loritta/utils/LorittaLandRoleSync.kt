package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.retrieveAllMessages
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.time.Instant
import kotlin.collections.set

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

			// Apply donators roles
			val payments = transaction(Databases.loritta) {
				Payment.find {
					(Payments.reason eq PaymentReason.DONATION) and (Payments.paidAt.isNotNull())
				}.toMutableList()
			}

			val donatorsPlusQuantity = mutableMapOf<Long, Double>()
			val donatorsPlusFirstDate = mutableMapOf<Long, Long>()
			val inactiveDonators = mutableSetOf<Long>()

			for (payment in payments) {
				if (payment.expiresAt ?: 0 >= System.currentTimeMillis()) {
					donatorsPlusQuantity[payment.userId] = payment.money.toDouble() + donatorsPlusQuantity.getOrDefault(payment.userId, 0.0)
					if (!donatorsPlusFirstDate.containsKey(payment.userId)) {
						donatorsPlusFirstDate[payment.userId] = payment.paidAt ?: 0L
					}
				} else {
					inactiveDonators.add(payment.userId)
				}
			}

			val donatorRole = originalGuild.getRoleById("364201981016801281")
			val superDonatorRole = originalGuild.getRoleById("463652112656629760")
			val megaDonatorRole = originalGuild.getRoleById("534659343656681474")
			val advertisementRole = originalGuild.getRoleById("619691791041429574")
			val inactiveRole = originalGuild.getRoleById("435856512787677214")

			val textChannel = originalGuild.getTextChannelById(Constants.THANK_YOU_DONATORS_CHANNEL_ID)

			val messages = runBlocking { textChannel!!.history.retrieveAllMessages() }

			for (member in originalGuild.members) {
				val roles = member.roles.toMutableSet()

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

					if (donated >= 39.99) {
						if (!roles.contains(advertisementRole))
							roles.add(advertisementRole)
					} else {
						if (roles.contains(advertisementRole))
							roles.remove(advertisementRole)
					}

					val helpedDays = (donated / 10)
					val plural = helpedDays != 1.0

					val text = "Obrigada a ${member.asMention} por doar para mim! <a:lori_happy:521721811298156558>\n\nGraças a doação de R$ ${"%.2f".format(donated)}, ${member.asMention} me ajudou a ficar mais ${"%.1f".format(helpedDays)} dia${if (plural) "s" else ""} online neste mês! **Você é incrível!** <:lori_hearts:519901735666581514>\n\nPara agradecer ${member.user.asMention}, reaja com <a:clapping:536170783257395202>! <:eu_te_moido:366047906689581085><a:clapping:536170783257395202>"

					val newEmbed = EmbedBuilder()
							.setTitle("\uD83D\uDE0A Obrigada!")
							.setThumbnail(member.user.effectiveAvatarUrl)
							.setDescription(text)
							.setColor(Constants.LORITTA_AQUA)
							.setFooter("Doador desde", "https://cdn.discordapp.com/emojis/515330130495799307.gif?v=1")
							.setTimestamp(Instant.ofEpochMilli(donatorsPlusFirstDate[member.user.idLong]!!))
							.build()

					val newMessage = MessageBuilder()
							.setContent(member.asMention)
							.setEmbed(
									newEmbed
							).build()

					val message = messages.firstOrNull { it.author.idLong == loritta.discordConfig.discord.clientId.toLong() && it.isMentioned(member) }

					if (message != null) {
						val embed = message.embeds.firstOrNull()
						if (embed == null) {
							message.delete().queue()
						} else {
							if (embed.description != newEmbed.description) {
								message.editMessage(newMessage).queue()
							}
						}
					} else {
						textChannel?.sendMessage(newMessage)?.queue {
							it.addReaction(
									"a:clapping:536170783257395202"
							).queue()
						}
					}
				} else {
					val filter = roles.filter { it.name.startsWith("\uD83C\uDFA8") }
					roles.removeAll(filter)

					if (roles.contains(advertisementRole))
						roles.remove(advertisementRole)

					if (roles.contains(donatorRole))
						roles.remove(donatorRole)

					if (roles.contains(superDonatorRole))
						roles.remove(superDonatorRole)

					if (roles.contains(megaDonatorRole))
						roles.remove(megaDonatorRole)

					if (inactiveDonators.contains(member.user.idLong)) {
						if (!roles.contains(inactiveRole)) {
							roles.add(inactiveRole)
						}
					} else
						roles.remove(inactiveRole)

					val message = messages.firstOrNull { it.author.idLong == loritta.discordConfig.discord.clientId.toLong() && it.contentRaw.startsWith(member.asMention) }

					message?.delete()?.queue()
				}

				if (!(roles.containsAll(member.roles) && member.roles.containsAll(roles))) {// Novos cargos foram adicionados
					logger.info("Alterando cargos de $member, cargos atuais são ${member.roles}, novos cargos serão $roles")
					member.guild.modifyMemberRoles(member, roles).queue()
				}
			}
		} catch (e: Exception) {
			logger.error("Erro ao sincronizar cargos!", e)
		}
	}

	fun synchronizeTranslatorsRoles(originalGuild: Guild) {
		val translatorRole = originalGuild.getRoleById("385579854336360449")

		logger.info("Processing translators roles...")
		val translators = loritta.locales.flatMap { it.value.getList("loritta.translationAuthors") }.distinct()

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