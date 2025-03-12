package net.perfectdreams.loritta.morenitta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.editMessageIfContentWasChanged
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal
import java.time.ZonedDateTime

object NitroBoostUtils {
	private val logger = KotlinLogging.logger {}
	private val REQUIRED_TO_RECEIVE_DREAM_BOOST = 19.00.toBigDecimal()

	internal fun createBoostTask(loritta: LorittaBot, config: LorittaConfig.DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = {
		while (true) {
			delay(60_000)
			logger.info { "Giving Sonhos to Donators..." }

			val boostAsDonationGuilds = config.boostEnabledGuilds.map { it.id }
			try {
				val donationKeySum = DonationKeys.value.sum()

				// get premium keys
				val guildsWithBoostFeature = loritta.newSuspendedTransaction {
					(ServerConfigs innerJoin DonationKeys).select(ServerConfigs.id, donationKeySum)
						.where { (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
						.groupBy(ServerConfigs.id)
						.having { donationKeySum greaterEq 99.99 }
						.toMutableList()
				}

				// Vantagem de key de doador: boosters ganham 2 sonhos por minuto
				for (guildWithBoostFeature in guildsWithBoostFeature) {
					if (guildWithBoostFeature[ServerConfigs.id].value in boostAsDonationGuilds) // Esses sonhos serão dados mais para frente, já que eles são considerados doadores
						continue

					val guild = loritta.lorittaShards.getGuildById(guildWithBoostFeature[ServerConfigs.id].value) ?: continue
					val boosters = guild.boosters

					logger.info { "Guild $guild has donation features enabled! Checking how many $boosters users can receive the reward..." }

					val todayAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
						.toOffsetDateTime()
						.withHour(0)
						.withMinute(0)
						.withSecond(0)
						.toInstant()
						.toEpochMilli()

					loritta.newSuspendedTransaction {
						// Only give the boosting reward if they got daily today
						val boostersThatGotDailyRecently = Dailies.select(Dailies.receivedById).where { 
							Dailies.receivedById inList boosters.map { it.user.idLong } and (Dailies.receivedAt greaterEq todayAtMidnight)
						}.groupBy(Dailies.receivedById)
							.map { it[Dailies.receivedById] }

						logger.info { "Guild $guild has donation features enabled! Giving sonhos to $boostersThatGotDailyRecently" }

						Profiles.update({ Profiles.id inList boostersThatGotDailyRecently }) {
							with(SqlExpressionBuilder) {
								it.update(money, money + 2)
							}
						}
					}
				}

				if (loritta.isMainInstance) {
					val moneySumId = Payments.money.sum()
					val mostPayingUsers = loritta.newSuspendedTransaction {
						Payments.select(Payments.userId, moneySumId)
							.where {
								Payments.paidAt.isNotNull() and (Payments.expiresAt greaterEq System.currentTimeMillis()) and
										((Payments.reason eq PaymentReason.DONATION) or (Payments.reason eq PaymentReason.SPONSORED))
							}
							.groupBy(Payments.userId)
							.having { moneySumId greaterEq REQUIRED_TO_RECEIVE_DREAM_BOOST }
							.orderBy(moneySumId, SortOrder.DESC)
							.toMutableList()
					}


					val deserveTheRewardUsers = mostPayingUsers.map { it[Payments.userId] }

					loritta.newSuspendedTransaction {
						for (profile in Profiles.selectAll().where { Profiles.id inList deserveTheRewardUsers }) {
							val userPayment = mostPayingUsers.firstOrNull { it[Payments.userId] == profile[Profiles.id].value }

							if (userPayment != null) {
								val roundedUpPayment = Math.ceil(userPayment[moneySumId]!!.toDouble())
								val howMuchShouldBeGiven = ((roundedUpPayment / 20.00) * 2) // Se doou 19.99, será (19.99 / 19.99) * 2 = 2 sonhos por segundo, se foi 39.99, será 4 sonhos, etc
								logger.info { "Giving $howMuchShouldBeGiven sonhos to ${profile[Profiles.id]}" }

								Profiles.update({ Profiles.id eq profile[Profiles.id] }) {
									it[money] = profile[money] + howMuchShouldBeGiven.toLong()
								}
							}
						}
					}
				}

				for (boostAsDonationGuildId in boostAsDonationGuilds) {
					val guild = loritta.lorittaShards.getGuildById(boostAsDonationGuildId) ?: continue

					// Remover key de boosts inválidos
					val nitroBoostPayments = loritta.newSuspendedTransaction {
						Payment.find {
							(Payments.gateway eq PaymentGateway.NITRO_BOOST)
						}.toMutableList()
					}

					val invalidNitroPayments = mutableListOf<Long>()

					for (nitroBoostPayment in nitroBoostPayments) {
						val metadata = nitroBoostPayment.metadata?.let { JsonParser.parseString(it) }
						val isFromThisGuild = metadata != null && metadata.obj["guildId"].nullLong == guild.idLong

						if (isFromThisGuild) {
							val member = guild.retrieveMemberOrNullById(nitroBoostPayment.userId)

							if (member == null || member.timeBoosted == null) {
								logger.warn { "Deleting Nitro Boost payment by ${nitroBoostPayment.userId} because user is not boosting the guild anymore! (is member null? ${member != null})" }
								invalidNitroPayments.add(nitroBoostPayment.userId)

								loritta.newSuspendedTransaction {
									nitroBoostPayment.delete()
								}
							}
						}
					}

					loritta.newSuspendedTransaction {
						DonationKey.find {
							(DonationKeys.expiresAt eq Long.MAX_VALUE) and (DonationKeys.value eq 20.00)
						}.toList()
					}.forEach {
						val metadata = it.metadata?.let { JsonParser.parseString(it) }
						val isFromThisGuild = metadata != null && metadata.obj["guildId"].nullLong == guild.idLong

						if (isFromThisGuild) {
							val member = guild.retrieveMemberOrNullById(it.userId)

							if (member == null || member.timeBoosted == null) {
								logger.warn { "Deleting donation key via Nitro Boost by ${it.userId} because user is not boosting the guild anymore! (is member null? ${member != null})" }

								loritta.newSuspendedTransaction {
									it.delete()
								}
							}
						}
					}
				}
			} catch (e: Exception) {
				logger.warn(e) { "Error while checking for guilds and stuff" }
			}
		}
	}

	internal fun updateValidBoostServers(loritta: LorittaBot, config: LorittaConfig.DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = update@{
		while (true) {
			delay(60_000)
			logger.info { "Updating Valid Guild Boost Messages..." }

			try {
				val channel = loritta.lorittaShards.getGuildMessageChannelById(config.channelId.toString())

				if (channel == null) {
					logger.warn { "Nitro Guilds Channel not found!" }
					continue
				}

				val message = channel.retrieveMessageById(config.messageId).await()

				if (message == null) {
					logger.warn { "Nitro Guilds Message not found!" }
					continue
				}

				val guilds = config.boostEnabledGuilds.map {
					async(loritta.coroutineDispatcher) {
						loritta.lorittaShards.queryGuildById(it.id)
					}
				}

				val await = guilds.awaitAll()

				val canBoostGuilds = mutableListOf<LorittaConfig.DonatorsOstentationConfig.BoostEnabledGuild>()

				await.filterNotNull().sortedByDescending { it["boostCount"].int }.forEach { queriedGuild ->
					val boostCount = queriedGuild["boostCount"].int
					logger.info { "Guild ${queriedGuild["name"]} has $boostCount boosts" }

					if (config.boostMax > boostCount)
						canBoostGuilds.add(config.boostEnabledGuilds.first { queriedGuild["id"].long == it.id })
				}

				val bestGuildToBoost = canBoostGuilds.sortedBy { it.priority }.take(2)

				val newContent = buildString {
					if (bestGuildToBoost.isEmpty())
						this.append("**Atualmente não tenho nenhum servidor disponível para você impulsar... volte mais tarde!** Cansado de esperar? Então compre premium no website da Loritta! https://loritta.website/donate <:lori_ameno:673868465433477126>")
					else {
						this.append("**Impulsione os seguintes servidores para ganhar as vantagens de doador, cada servidor que você impulsionar contam como R$ 20 doados! Lembre-se que você apenas deve dar um boost por servidor!** <:lori_feliz:519546310978830355>\n\n**Não se esqueça de verificar se o servidor possui menos de 40 boosts, já que você não receberá a vantagem caso tenha mais que 40!**\n\nApós dar o boost, envie uma mensagem no chat para receber as vantagens. Boost é muito chato para você? Então compre premium no website da Loritta! <https://loritta.website/donate>")
						for (guild in bestGuildToBoost) {
							this.append('\n')
							this.append("https://discord.gg/${guild.inviteId}")
						}
					}
				}

				message.editMessageIfContentWasChanged(newContent)
			} catch (e: Exception) {
				logger.info(e) { "Something went wrong while updating the nitro servers messages" }
			}
		}
	}

	suspend fun onBoostActivate(loritta: LorittaBot, member: Member) {
		val guild = member.guild

		logger.info { "Enabling donation features via boost for $member in $guild!" }

		val now = System.currentTimeMillis()

		loritta.newSuspendedTransaction {
			// Gerar pagamento
			Payment.new {
				this.userId = member.idLong
				this.gateway = PaymentGateway.NITRO_BOOST
				this.reason = PaymentReason.DONATION
				this.createdAt = now
				this.paidAt = now
				this.money = BigDecimal(20.00)
				this.expiresAt = Long.MAX_VALUE // Nunca!
				this.metadata = jsonObject(
					"guildId" to member.guild.idLong
				).toString()
			}

			// Gerar key de doação
			DonationKey.new {
				this.userId = member.idLong
				this.value = 20.00
				this.expiresAt = Long.MAX_VALUE // Nunca!
				this.metadata = jsonObject(
					"guildId" to member.guild.idLong
				).toString()
			}
		}

		// Fim!
		try {
			member.user.openPrivateChannel().await().sendMessageEmbeds(
				EmbedBuilder()
					.setTitle("Obrigada por ativar o seu boost! ${Emotes.LORI_HAPPY}")
					.setDescription(
						"Obrigada por ativar o seu Nitro Boost no meu servidor! ${Emotes.LORI_NITRO_BOOST}\n\nA cada dia eu estou mais próxima de virar uma digital influencer de sucesso, graças a sua ajuda! ${Emotes.LORI_HAPPY}\n\nAh, e como agradecimento por você ter ativado o seu boost no meu servidor, você irá receber todas as minhas vantagens de quem doa 19,99 reais! (Até você desativar o seu boost... espero que você não desative... ${Emotes.LORI_CRYING})\n\nContinue sendo incrível!"
					)
					.setImage("https://stuff.loritta.website/loritta-boost-raspoza.png")
					.setColor(Constants.LORITTA_AQUA)
					.build()
			).await()
		} catch (e: Exception) {}
	}

	suspend fun onBoostDeactivate(loritta: LorittaBot, member: Member) {
		val guild = member.guild

		logger.info { "Disabling donation features via boost for $member in $guild!"}

		loritta.newSuspendedTransaction {
			Payment.find {
				(Payments.userId eq member.idLong) and (Payments.gateway eq PaymentGateway.NITRO_BOOST)
			}.firstOrNull {
				val metadata = it.metadata?.let { JsonParser.parseString(it) }
				metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
			}?.delete()

			DonationKey.find {
				(DonationKeys.userId eq member.idLong) and (DonationKeys.expiresAt eq Long.MAX_VALUE) and (DonationKeys.value eq 20.00)
			}.firstOrNull {
				val metadata = it.metadata?.let { JsonParser.parseString(it) }
				metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
			}?.apply {
				this.expiresAt = System.currentTimeMillis() // Ou seja, a key estará expirada
			}
		}
	}
}