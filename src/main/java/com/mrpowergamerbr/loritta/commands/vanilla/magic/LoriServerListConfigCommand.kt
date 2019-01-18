package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanType
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.MercadoPagoCallbackController
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.utils.giveaway.payments.PaymentGateway
import net.perfectdreams.loritta.utils.giveaway.payments.PaymentReason
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LoriServerListConfigCommand : AbstractCommand("lslc", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)
		val arg3 = context.rawArgs.getOrNull(3)

		// Sub-comandos que só o Dono pode usar
		if (context.userHandle.id == Loritta.config.ownerId) {
			if (arg0 == "set_dreams" && arg1 != null && arg2 != null) {
				val user = context.getUserAt(2)!!
				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq user.idLong }) {
						it[money] = arg1.toDouble()
					}
				}

				context.reply(
						LoriReply(
								"Sonhos de ${user.asMention} foram editados com sucesso!"
						)
				)
				return
			}

			if (arg0 == "generate_payment" && arg1 != null && arg2 != null) {
				transaction(Databases.loritta) {
					Payment.new {
						this.createdAt = System.currentTimeMillis()
						this.discount = 0.0
						this.paidAt = System.currentTimeMillis()
						this.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS
						this.userId = arg1.toLong()
						this.gateway = PaymentGateway.OTHER
						this.reason = PaymentReason.DONATION
						this.money = arg2.toBigDecimal()
					}
				}

				context.reply(
						LoriReply(
								"Pagamento criado com sucesso!"
						)
				)
				return
			}

			if (arg0 == "generate_key" && arg1 != null && arg2 != null) {
				transaction(Databases.loritta) {
					DonationKey.new {
						this.userId = arg1.toLong()
						this.expiresAt = System.currentTimeMillis() + 2_764_800_000
						this.value = arg2.toDouble()
					}
				}

				context.reply(
						LoriReply(
								"Key criada com sucesso!"
						)
				)
				return
			}

			if (arg0 == "allow_any_payment" && arg1 != null) {
				MercadoPagoCallbackController.allowAnyPayment = arg1.toBoolean()

				context.reply(
						LoriReply(
								"Todos os pagamentos serão aprovados sem confirmação? ${MercadoPagoCallbackController.allowAnyPayment}"
						)
				)
				return
			}

			if (arg0 == "inspect_donations" && arg1 != null) {
				val id = arg1.toLong()

				val moneyFromDonations = loritta.getActiveMoneyFromDonations(id)

				context.reply(
						LoriReply(
								"<@${id}> possui **R$ ${moneyFromDonations}** ativos"
						)
				)
				return
			}

			if (arg0 == "commit_bans") {
				val replies = mutableListOf<LoriReply>()
				replies.add(
						LoriReply(
								"**Lista de usuários a serem banidos *GLOBALMENTE*...**",
								Emotes.DISCORD_ONLINE
						)
				)

				loritta.networkBanManager.notVerifiedEntries.forEach {
					if (replies.sumBy { it.build(context).length } >= 2000) {
						context.reply(*replies.toTypedArray())
						replies.clear()
					}

					val user = lorittaShards.getUserById(it.id) ?: return

					val typeEmote = when {
						user.isBot -> Emotes.DISCORD_BOT_TAG
						else -> Emotes.DISCORD_WUMPUS_BASIC
					}

					val mutualGuilds = lorittaShards.getMutualGuilds(user)
					val serverConfigs = loritta.serversColl.find(
							Filters.and(
									Filters.`in`("_id", mutualGuilds.map { it.id }),
									Filters.eq("moderationConfig.useLorittaBansNetwork", true)
							)
					).toMutableList()

					replies.add(
							LoriReply(
									"$typeEmote `${user.name.stripCodeMarks()}#${user.discriminator}` (${user.id}/${it.type.name}) — ${mutualGuilds.size} servidores compartilhados (${serverConfigs.size} com os bans globais ativados)",
									mentionUser = false
							)
					)
				}

				context.reply(*replies.toTypedArray())
				replies.clear()

				val message = context.reply(
						LoriReply(
								"Veja se tudo está correto, caso esteja, veja de novo e caso realmente esteja certo, aperte no ✅",
								Emotes.DISCORD_DO_NOT_DISTURB
						)
				)

				message.addReaction("✅").queue()
				message.addReaction("error:412585701054611458").queue()

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.name == "✅") {
						loritta.networkBanManager.notVerifiedEntries.forEach {
							loritta.networkBanManager.addBanEntry(it)
						}
						loritta.networkBanManager.notVerifiedEntries.clear()
						context.reply(
								LoriReply(
										"Todos os usuários da lista foram adicionados na lista de bans globais, yay!"
								)
						)
					} else {
						loritta.networkBanManager.notVerifiedEntries.clear()
						context.reply(
								LoriReply(
										"A lista de bans não verificados foi limpa, whoosh!"
								)
						)
					}
				}
				return
			}

			if (arg0 == "set_sponsor" && arg1 != null && arg2 != null && arg3 != null) {
				val guild = lorittaShards.getGuildById(arg1)!!
				val serverConfig = loritta.getServerConfigForGuild(guild.id)
				val isSponsor = arg2.toBoolean()

				serverConfig.serverListConfig.isSponsored = isSponsor
				serverConfig.serverListConfig.sponsorPaid = arg3.toDouble()

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				serverConfig.serverListConfig.sponsoredUntil = rawArgs.joinToString(" ").convertToEpochMillisRelativeToNow()

				loritta save serverConfig

				context.reply(
						LoriReply(
								"Servidor `${guild.name}` foi marcado como patrociado até `${serverConfig.serverListConfig.sponsoredUntil.humanize(locale)}`"
						)
				)
			}

			if (arg0 == "generate_key" && arg1 != null && arg2 != null) {
				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)

				val args = rawArgs.joinToString(" ")
						.split("|")
						.map { it.trim() }
						.toMutableList()

				val price = args[0].toDouble()
				val reason = args[1]

				val time = args[2].convertToEpochMillisRelativeToNow()

				val key = RandomStringUtils.random(32, 0, 66, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@!$&".toCharArray())

				val premiumKey = PremiumKey(
						key,
						reason,
						time,
						price
				)

				loritta.premiumKeys.add(premiumKey)

				loritta.savePremiumKeys()

				context.reply(
						LoriReply(
								"Key gerada! `${premiumKey.name}`"
						)
				)
			}
		}

		// Sub-comandos que o dono e os Supervisores de Lori podem usar
		if (context.userHandle.id == Loritta.config.ownerId || context.userHandle.lorittaSupervisor) {
			if (arg0 == "network_ban" && arg1 != null && arg2 != null && arg3 != null) {
				val userId = arg1
				var guildId = arg2
				if (guildId == "null")
					guildId = null

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				loritta.networkBanManager.addNonVerifiedEntry(
						NetworkBanEntry(
								userId,
								guildId,
								NetworkBanType.valueOf(arg3),
								rawArgs.joinToString(" ")
						)
				)

				val user = lorittaShards.retrieveUserById(userId) ?: run {
					context.reply(
							LoriReply(
									"Usuário ${userId} não existe!"
							)
					)
					return
				}

				context.reply(
						LoriReply(
								"Usuário $userId (`${user.name}`) adicionado na lista de usuários a serem banidos na Loritta Bans Network! Use `+lslc commit_bans` para confirmar"
						)
				)
			}

			if (arg0 == "network_unban" && arg1 != null) {
				val userId = arg1

				val filtered = loritta.networkBanManager.networkBannedUsers.filter { it.id != userId }
						.toMutableList()

				loritta.networkBanManager.networkBannedUsers = filtered

				loritta.networkBanManager.saveNetworkBannedUsers()

				context.reply(
						LoriReply(
								"Usuário desbanido na Loritta Bans Network!"
						)
				)
			}
		}
	}
}