package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlacklistedGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.GuildProfile
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EconomyConfig
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.utils.isLorittaSupervisor
import net.perfectdreams.loritta.serializable.StoredDivineInterventionSonhosTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class LoriServerListConfigCommand(loritta: LorittaBot) : AbstractCommand(loritta, "lslc", category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)
		val arg3 = context.rawArgs.getOrNull(3)

		// Sub-comandos que só o Dono pode usar
		if (context.loritta.isOwner(context.userHandle.id)) {
			if (arg0 == "inject_economy") {
				val config = context.loritta.getOrCreateServerConfig(context.guild.idLong)

				loritta.pudding.transaction {
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
				loritta.pudding.transaction {
					val profile = GuildProfile.find { (GuildProfiles.guildId eq context.guild.idLong) and (GuildProfiles.userId eq arg1!!.toLong()) }.firstOrNull()
					profile?.money = arg2?.toDouble()?.toBigDecimal() ?: 0.0.toBigDecimal()
				}

				context.reply(
						"Quantidade alterada com sucesso!!"
				)
				return
			}
			if (arg0 == "set_update_post") {
				val shards = context.loritta.config.loritta.clusters.instances

				val jobs = shards.map {
					GlobalScope.async(context.loritta.coroutineDispatcher) {
						try {
							val body = HttpRequest.get("${it.getUrl(loritta)}/api/v1/loritta/update")
									.userAgent(context.loritta.lorittaCluster.getUserAgent(loritta))
									.header("Authorization", context.loritta.lorittaInternalApiKey.name)
									.connectTimeout(context.loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(context.loritta.config.loritta.clusterReadTimeout)
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

							JsonParser.parseString(
									body
							)
						} catch (e: Exception) {
							logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
							throw ClusterOfflineException(it.id, it.name)
						}
					}
				}

				jobs.awaitAll()

				context.reply(
						"Enviado patch data!"
				)
				return
			}

			if (arg0 == "set_dreams" && arg1 != null && arg2 != null) {
				val user = context.getUserAt(2)!!
				loritta.pudding.transaction {
					Profiles.update({ Profiles.id eq user.idLong }) {
						it[money] = arg1.toLong()
					}
				}

				context.reply(
                        LorittaReply(
                                "Sonhos de ${user.asMention} foram editados com sucesso!"
                        )
				)
				return
			}
		}

		// Sub-comandos que o dono e os Supervisores de Lori podem usar
		if (context.loritta.isOwner(context.userHandle.id) || context.userHandle.isLorittaSupervisor(context.loritta.lorittaShards)) {
			if (arg0 == "generate_payment" && arg1 != null && arg2 != null) {
				loritta.pudding.transaction {
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
					LorittaReply(
						"Pagamento criado com sucesso!"
					)
				)
				return
			}

			if (arg0 == "generate_key" && arg1 != null && arg2 != null) {
				loritta.pudding.transaction {
					DonationKey.new {
						this.userId = arg1.toLong()
						this.expiresAt = System.currentTimeMillis() + 2_764_800_000
						this.value = arg2.toDouble()
					}
				}

				context.reply(
					LorittaReply(
						"Key criada com sucesso!"
					)
				)
				return
			}

			if (arg0 == "add_dreams" && arg1 != null && arg2 != null) {
				val user = context.getUserAt(2)!!
				loritta.pudding.transaction {
					Profiles.update({ Profiles.id eq user.idLong }) {
						with(SqlExpressionBuilder) {
							it.update(money, money + arg1.toLong())
						}
					}

					// Cinnamon transaction system
					SimpleSonhosTransactionsLogUtils.insert(
						user.idLong,
						Instant.now(),
						TransactionType.DIVINE_INTERVENTION,
						arg1.toLong(),
						StoredDivineInterventionSonhosTransaction(
							DivineInterventionTransactionEntryAction.ADDED_SONHOS,
							context.userHandle.idLong,
							null
						)
					)
				}

				context.reply(
					LorittaReply(
						"Sonhos de ${user.asMention} foram editados com sucesso!"
					)
				)
				return
			}

			if (arg0 == "remove_dreams" && arg1 != null && arg2 != null) {
				val user = context.getUserAt(2)!!
				loritta.pudding.transaction {
					Profiles.update({ Profiles.id eq user.idLong }) {
						with(SqlExpressionBuilder) {
							it.update(money, money - arg1.toLong())
						}
					}

					// Cinnamon transaction system
					SimpleSonhosTransactionsLogUtils.insert(
						user.idLong,
						Instant.now(),
						TransactionType.DIVINE_INTERVENTION,
						arg1.toLong(),
						StoredDivineInterventionSonhosTransaction(
							DivineInterventionTransactionEntryAction.REMOVED_SONHOS,
							context.userHandle.idLong,
							null
						)
					)
				}

				context.reply(
					LorittaReply(
						"Sonhos de ${user.asMention} foram editados com sucesso!"
					)
				)
				return
			}

			if (arg0 == "guild_ban" && arg1 != null) {
				val guildId = arg1.toLong()

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				loritta.pudding.transaction {
					BlacklistedGuilds.insert {
						it[BlacklistedGuilds.id] = EntityID(guildId, BlacklistedGuilds)
						it[BlacklistedGuilds.bannedAt] = System.currentTimeMillis()
						it[BlacklistedGuilds.reason] = rawArgs.joinToString(" ")
					}
				}

				context.reply(
                        LorittaReply(
                                "Guild banida!"
                        )
				)
			}

			if (arg0 == "guild_unban" && arg1 != null) {
				val guildId = arg1.toLong()

				val rawArgs = context.rawArgs.toMutableList()
				rawArgs.removeAt(0)
				rawArgs.removeAt(0)

				loritta.pudding.transaction {
					BlacklistedGuilds.deleteWhere {
						BlacklistedGuilds.id eq guildId
					}
				}

				context.reply(
                        LorittaReply(
                                "Guild desbanida!"
                        )
				)
			}

			if (arg0 == "search_user") {
				val split = context.rawArgs.toMutableList().drop(1).joinToString(" ").split("#")
				val name = split[0]
				val discriminator = split.getOrNull(1)

				val allUsers = context.loritta.lorittaShards.searchUserInAllLorittaClusters(name, discriminator, isRegExPattern = true)

				val strBuilder = StringBuilder()

				allUsers.forEach {
					val name = it["name"].string
					val discriminator = it["discriminator"].string
					val id = it["id"].long

					strBuilder.append("`${name}#${discriminator}` (`${id}`)\n")
				}

				if (strBuilder.length > 2000) {
					context.sendFile(strBuilder.toString().toByteArray(Charsets.UTF_8).inputStream(), "users.txt", "São tantos resultados que eu decidi colocar em um arquivo de texto! ^-^")
					return
				}

				if (strBuilder.isEmpty()) {
					context.reply(
                            LorittaReply(
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

				val allGuilds = context.loritta.lorittaShards.searchGuildInAllLorittaClusters(pattern)

				val strBuilder = StringBuilder()

				allGuilds.forEach {
					val name = it["name"].string
					val id = it["id"].long

					strBuilder.append("`${name}` (`${id}`)\n")
				}

				if (strBuilder.length > 2000) {
					context.sendFile(strBuilder.toString().toByteArray(Charsets.UTF_8).inputStream(), "guilds.txt", "São tantos resultados que eu decidi colocar em um arquivo de texto! ^-^")
					return
				}

				if (strBuilder.isEmpty()) {
					context.reply(
                            LorittaReply(
                                    "Nenhuma guild se encaixa na pesquisa que você realizou, sorry ;w;",
                                    Constants.ERROR
                            )
					)
					return
				}

				context.sendMessage(strBuilder.toString())
				return
			}

			if (arg0 == "economy") {
				val value = arg1!!.toBoolean()

				val shards = context.loritta.config.loritta.clusters.instances

				shards.map {
					GlobalScope.async(context.loritta.coroutineDispatcher) {
						try {
							val body = HttpRequest.post("${it.getUrl(loritta)}/api/v1/loritta/action/economy")
									.userAgent(context.loritta.lorittaCluster.getUserAgent(loritta))
									.header("Authorization", context.loritta.lorittaInternalApiKey.name)
									.connectTimeout(context.loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(context.loritta.config.loritta.clusterReadTimeout)
									.send(
											gson.toJson(
													jsonObject(
															"enabled" to value
													)
											)
									)
									.body()

							JsonParser.parseString(
									body
							)
						} catch (e: Exception) {
							logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
							throw ClusterOfflineException(it.id, it.name)
						}
					}
				}

				context.reply(
                        LorittaReply(
                                "Alterando status de economia em todos os clusters..."
                        )
				)
				return
			}

			if (arg0 == "inspect_donations" && arg1 != null) {
				val id = arg1.toLong()

				val moneyFromDonations = context.loritta.getActiveMoneyFromDonations(id)

				context.reply(
					LorittaReply(
						"<@${id}> possui **R$ ${moneyFromDonations}** ativos"
					)
				)
				return
			}
		}
	}
}