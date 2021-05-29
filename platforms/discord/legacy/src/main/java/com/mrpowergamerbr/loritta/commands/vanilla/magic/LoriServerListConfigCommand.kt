package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaShards
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.lorittaSupervisor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.dao.servers.moduleconfigs.EconomyConfig
import net.perfectdreams.loritta.tables.BlacklistedGuilds
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LoriServerListConfigCommand : AbstractCommand("lslc", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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
					GlobalScope.async(loritta.coroutineDispatcher) {
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
				transaction(Databases.loritta) {
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

			if (arg0 == "add_dreams" && arg1 != null && arg2 != null) {
				val user = context.getUserAt(2)!!
				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq user.idLong }) {
						with(SqlExpressionBuilder) {
							it.update(money, money + arg1.toLong())
						}
					}
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
				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq user.idLong }) {
						with(SqlExpressionBuilder) {
							it.update(money, money - arg1.toLong())
						}
					}
				}

				context.reply(
                        LorittaReply(
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
                        LorittaReply(
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
                        LorittaReply(
                                "Key criada com sucesso!"
                        )
				)
				return
			}
		}

		// Sub-comandos que o dono e os Supervisores de Lori podem usar
		if (loritta.config.isOwner(context.userHandle.id) || context.userHandle.lorittaSupervisor) {
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

				transaction(Databases.loritta) {
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

				val allUsers = lorittaShards.searchUserInAllLorittaClusters(name, discriminator, isRegExPattern = true)

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

				val allGuilds = lorittaShards.searchGuildInAllLorittaClusters(pattern)

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

				val shards = loritta.config.clusters

				shards.map {
					GlobalScope.async(loritta.coroutineDispatcher) {
						try {
							val body = HttpRequest.post("https://${it.getUrl()}/api/v1/loritta/action/economy")
									.userAgent(loritta.lorittaCluster.getUserAgent())
									.header("Authorization", loritta.lorittaInternalApiKey.name)
									.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(loritta.config.loritta.clusterReadTimeout)
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
							LorittaShards.logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
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

				val moneyFromDonations = loritta.getActiveMoneyFromDonationsAsync(id)

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