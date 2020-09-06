package net.perfectdreams.loritta.plugin.donatorsostentation

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.editMessageIfContentWasChanged
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal

object NitroBoostUtils {
	private val logger = KotlinLogging.logger {}
	private val REQUIRED_TO_RECEIVE_DREAM_BOOST = 19.00.toBigDecimal()

	internal fun createBoostTask(config: DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = {
		while (true) {
			delay(60_000)
			logger.info { "Giving Sonhos to Donators..." }

			val boostAsDonationGuilds = config.boostEnabledGuilds.map { it.id }
			try {
				// get premium keys
				val guildsWithBoostFeature = loritta.newSuspendedTransaction {
					(ServerConfigs innerJoin DonationKeys).slice(ServerConfigs.id, DonationKeys.expiresAt, DonationKeys.value)
							.select {
								DonationKeys.value greaterEq 99.99 and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
							}.toMutableList()
				}

				// Vantagem de key de doador: boosters ganham 2 sonhos por minuto
				for (guildWithBoostFeature in guildsWithBoostFeature) {
					if (guildWithBoostFeature[ServerConfigs.id].value in boostAsDonationGuilds) // Esses sonhos serão dados mais para frente, já que eles são considerados doadores
						continue

					val guild = lorittaShards.getGuildById(guildWithBoostFeature[ServerConfigs.id].value) ?: continue
					val boosters = guild.boosters

					logger.info { "Guild $guild has donation features enabled! Giving sonhos to $boosters" }

					loritta.newSuspendedTransaction {
						Profiles.update({ Profiles.id inList boosters.map { it.user.idLong } }) {
							with(SqlExpressionBuilder) {
								it.update(money, money + 2)
							}
						}
					}
				}

				if (LorittaLauncher.loritta.isMaster) {
					val moneySumId = Payments.money.sum()
					val mostPayingUsers = loritta.newSuspendedTransaction {
						Payments.slice(Payments.userId, moneySumId)
								.select {
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
						for (profile in Profiles.select { Profiles.id inList deserveTheRewardUsers }) {
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
					val guild = lorittaShards.getGuildById(boostAsDonationGuildId) ?: continue

					// Remover key de boosts inválidos
					val nitroBoostPayments = loritta.newSuspendedTransaction {
						Payment.find {
							(Payments.gateway eq PaymentGateway.NITRO_BOOST)
						}.toMutableList()
					}

					val invalidNitroPayments = mutableListOf<Long>()

					for (nitroBoostPayment in nitroBoostPayments) {
						val metadata = nitroBoostPayment.metadata
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
						val metadata = it.metadata
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

	internal fun updateValidBoostServers(config: DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = update@{
		while (true) {
			delay(60_000)
			logger.info { "Updating Valid Guild Boost Messages..." }

			try {
				val channel = lorittaShards.getTextChannelById(config.channelId.toString())

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
						lorittaShards.queryGuildById(it.id)
					}
				}

				val await = guilds.awaitAll()

				val canBoostGuilds = mutableListOf<DonatorsOstentationConfig.BoostEnabledGuild>()

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
						this.append("**Impulsione os seguintes servidores para ganhar as vantagens de doador, cada servidor que você impulsionar contam como R$ 20 doados! Lembre-se que você apenas deve dar um boost por servidor!** <:lori_feliz:519546310978830355>\n\nBoost é muito chato para você? Então compre premium no website da Loritta! <https://loritta.website/donate>")
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

	suspend fun onBoostActivate(member: Member) {
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
				)
			}

			// Gerar key de doação
			DonationKey.new {
				this.userId = member.idLong
				this.value = 20.00
				this.expiresAt = Long.MAX_VALUE // Nunca!
				this.metadata = jsonObject(
						"guildId" to member.guild.idLong
				)
			}
		}

		// Fim!
		try {
			member.user.openPrivateChannel().await().sendMessage(
					EmbedBuilder()
							.setTitle("Obrigada por ativar o seu boost! ${Emotes.LORI_HAPPY}")
							.setDescription(
									"Obrigada por ativar o seu Nitro Boost no meu servidor! ${Emotes.LORI_NITRO_BOOST}\n\nA cada dia eu estou mais próxima de virar uma digital influencer de sucesso, graças a sua ajuda! ${Emotes.LORI_HAPPY}\n\nAh, e como agradecimento por você ter ativado o seu boost no meu servidor, você irá receber todas as minhas vantagens de quem doa 19,99 reais! (Até você desativar o seu boost... espero que você não desative... ${Emotes.LORI_CRYING})\n\nContinue sendo incrível!"
							)
							.setImage("https://loritta.website/assets/img/fanarts/Loritta_-_Raspoza.png")
							.setColor(Constants.LORITTA_AQUA)
							.build()
			).await()
		} catch (e: Exception) {}
	}

	suspend fun onBoostDeactivate(member: Member) {
		val guild = member.guild

		logger.info { "Disabling donation features via boost for $member in $guild!"}

		loritta.newSuspendedTransaction {
			Payment.find {
				(Payments.userId eq member.idLong) and (Payments.gateway eq PaymentGateway.NITRO_BOOST)
			}.firstOrNull {
				val metadata = it.metadata
				metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
			}?.delete()

			DonationKey.find {
				(DonationKeys.userId eq member.idLong) and (DonationKeys.expiresAt eq Long.MAX_VALUE) and (DonationKeys.value eq 20.00)
			}.firstOrNull {
				val metadata = it.metadata
				metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
			}?.apply {
				this.expiresAt = System.currentTimeMillis() // Ou seja, a key estará expirada
			}
		}
	}
}