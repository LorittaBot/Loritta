package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.misc.PingCommand
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanType
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.MercadoPagoCallbackController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.dao.EconomyConfig
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.tables.BlacklistedGuilds
import net.perfectdreams.loritta.tables.BlacklistedUsers
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
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
		if (loritta.config.isOwner(context.userHandle.id)) {
			if (arg0 == "inject_economy") {
				val config = loritta.getOrCreateServerConfig(context.guild.idLong)

				transaction(Databases.loritta) {
					config.economyConfig = EconomyConfig.new {
						this.enabled = true
						this.economyName = "LoriCoin"
						this.economyNamePlural = "LoriCoins"
						this.sonhosExchangeEnabled = true
						this.exchangeRate = 1.0
						this.sonhosExchangeEnabled = true
						this.realMoneyToEconomyRate = 1.0
					}
				}

				context.reply(
						"Deve ter dado certo, yay"
				)
				return
			}
			if (arg0 == "set_local_money") {
				transaction(Databases.loritta) {
					val profile = GuildProfile.find { (GuildProfiles.guildId eq context.guild.idLong) and (GuildProfiles.userId eq arg1!!.toLong()) }.firstOrNull()
					profile?.money = arg2?.toDouble()?.toBigDecimal() ?: 0.0.toBigDecimal()
				}

				context.reply(
						"Quantidade alterada com sucesso!!"
				)
				return
			}
			if (arg0 == "set_update_post") {
				val shards = loritta.config.clusters

				val jobs = shards.map {
					GlobalScope.async {
						try {
							val body = HttpRequest.get("https://${it.getUrl()}/api/v1/loritta/update")
									.userAgent(loritta.lorittaCluster.getUserAgent())
									.header("Authorization", loritta.lorittaInternalApiKey.name)
									.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(loritta.config.loritta.clusterReadTimeout)
									.send(
											gson.toJson(
													jsonObject(
															"type" to "setPatchNotesPost",
															"patchNotesPostId" to arg1,
															"expiresAt" to arg2!!.toLong()
													)
											)
									)
									.body()

							jsonParser.parse(
									body
							)
						} catch (e: Exception) {
							logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
							throw PingCommand.ShardOfflineException(it.id, it.name)
						}
					}
				}

				jobs.awaitAll()

				context.reply(
						"Enviado patch data!"
				)
				return
			}
			if (arg0 == "search_user") {
				val pattern = context.rawArgs.toMutableList().drop(1).joinToString(" ")

				val allUsers = lorittaShards.searchUserInAllLorittaClusters(pattern)

				val strBuilder = StringBuilder()

				allUsers.forEach {
					val name = it["name"].string
					val discriminator = it["discriminator"].string
					val id = it["id"].long

					strBuilder.append("`${name}#${discriminator}` (`${id}`)\n")
				}

				if (strBuilder.length > 2000) {
					context.reply(
							LoriReply(
									"Tem tanto usuário na lista que eu não vou conseguir mostrar, a mensagem está grande demais! Sorry ;w;",
									Constants.ERROR
							)
					)
					return
				}

				if (strBuilder.isEmpty()) {
					context.reply(
							LoriReply(
									"Nenhum usuário se encaixa na pesquisa que você realizou, sorry ;w;",
									Constants.ERROR
							)
					)
					return
				}

				context.sendMessage(strBuilder.toString())
				return
			}

			if (arg0 == "search_guild") {
				val pattern = context.rawArgs.toMutableList().drop(1).joinToString(" ")

				val allGuilds = lorittaShards.searchGuildInAllLorittaClusters(pattern)

				val strBuilder = StringBuilder()

				allGuilds.forEach {
					val name = it["name"].string
					val id = it["id"].long

					strBuilder.append("`${name}` (`${id}`)\n")
				}

				if (strBuilder.length > 2000) {
					context.reply(
							LoriReply(
									"Tem tanta guild na lista que eu não vou conseguir mostrar, a mensagem está grande demais! Sorry ;w;",
									Constants.ERROR
							)
					)
					return
				}

				if (strBuilder.isEmpty()) {
					context.reply(
							LoriReply(
									"Nenhuma guild se encaixa na pesquisa que você realizou, sorry ;w;",
									Constants.ERROR
							)
					)
					return
				}

				context.sendMessage(strBuilder.toString())
				return
			}

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
								Emotes.ONLINE
						)
				)

				loritta.networkBanManager.notVerifiedEntries.forEach {
					if (replies.sumBy { it.build(context).length } >= 2000) {
						context.reply(*replies.toTypedArray())
						replies.clear()
					}

					val user = lorittaShards.retrieveUserById(it.id) ?: return@forEach

					val typeEmote = when {
						user.isBot -> Emotes.BOT_TAG
						else -> Emotes.WUMPUS_BASIC
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
								Emotes.DO_NOT_DISTURB
						)
				)

				message.addReaction("✅").queue()
				message.addReaction("error:412585701054611458").queue()

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.isEmote("✅")) {
						loritta.networkBanManager.notVerifiedEntries.forEach { entry ->
							loritta.networkBanManager.addBanEntry(entry)
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
		}

		// Sub-comandos que o dono e os Supervisores de Lori podem usar
		if (loritta.config.isOwner(context.userHandle.id) || context.userHandle.lorittaSupervisor) {
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
								userId.toLong(),
								guildId?.toLong(),
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

				transaction(Databases.loritta) {
					BlacklistedUsers.deleteWhere {
						BlacklistedUsers.id eq userId.toLong()
					}
				}

				context.reply(
						LoriReply(
								"Usuário desbanido na Loritta Bans Network!"
						)
				)
			}

			if (arg0 == "guild_ban" && arg1 != null) {
				val guildId = arg1.toLong()

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				transaction(Databases.loritta) {
					BlacklistedGuilds.insert {
						it[BlacklistedGuilds.id] = EntityID(guildId, BlacklistedGuilds)
						it[BlacklistedGuilds.bannedAt] = System.currentTimeMillis()
						it[BlacklistedGuilds.reason] = rawArgs.joinToString(" ")
					}
				}

				context.reply(
						LoriReply(
								"Guild banida!"
						)
				)
			}

			if (arg0 == "guild_unban" && arg1 != null) {
				val guildId = arg1.toLong()

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				transaction(Databases.loritta) {
					BlacklistedGuilds.deleteWhere {
						BlacklistedGuilds.id eq guildId
					}
				}

				context.reply(
						LoriReply(
								"Guild desbanida!"
						)
				)
			}
		}
	}
}