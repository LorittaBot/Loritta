package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.economy.PagarCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BannedIps
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.tables.WhitelistedTransactionIds
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class PostTransferBalanceRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/transfer-balance") {
	companion object {
		private val mutex = Mutex()
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		loritta as Loritta
		val json = jsonParser.parse(call.receiveText())
		val giverId = json["giverId"].long
		val receiverId = json["receiverId"].long
		val howMuch = json["howMuch"].long

		logger.info { "Initializing transaction between $giverId and $receiverId, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
		mutex.withLock {
			val receiverProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(receiverId)
			val giverProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(giverId)

			if (howMuch > giverProfile.money) {
				call.respondJson(
						jsonObject(
								"status" to PagarCommand.PayStatus.NOT_ENOUGH_MONEY.toString()
						)
				)
				return@withLock
			}

			// Verificação de alt accounts
			// Se o usuário tentar transferir para o mesmo user, dê um ban automático
			val todayAtMidnight = Instant.now()
					.atZone(ZoneId.of("America/Sao_Paulo"))
					.toOffsetDateTime()
					.withHour(0)
					.withMinute(0)
					.withSecond(0)
					.toInstant()
					.toEpochMilli()

			val lastReceiverDailyAt = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq receiverId and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt to false)
						.firstOrNull()
			}

			val lastGiverDailyAt = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq giverId and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt to false)
						.firstOrNull()
			}

			val beforeGiver = giverProfile.money
			val beforeReceiver = receiverProfile.money

			val activeMoneyFromDonations = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonations(giverId)
			val taxBypass = UserPremiumPlans.getPlanFromValue(activeMoneyFromDonations).noPaymentTax

			val taxedMoney = if (taxBypass) { 0.0 } else { Math.ceil(PagarCommand.TRANSACTION_TAX * howMuch.toDouble()) }
			val finalMoney = (howMuch - taxedMoney).toLong()

			if (lastReceiverDailyAt != null && lastGiverDailyAt != null) {
				val receiverDailyIp = lastReceiverDailyAt[Dailies.ip]
				val giverDailyIp = lastGiverDailyAt[Dailies.ip]

				if (receiverDailyIp == giverDailyIp) {
					val isWhitelisted = transaction(Databases.loritta) {
						WhitelistedTransactionIds.select {
							WhitelistedTransactionIds.userId eq giverId or (WhitelistedTransactionIds.userId eq receiverId)
						}.firstOrNull()
					}

					if (isWhitelisted != null) {
						logger.warn { "Same IP detected for $receiverId and $giverId ($receiverDailyIp), but the IP is whitelisted! Ignoring..." }
					} else {
						logger.warn { "Same IP detected for $receiverId and $giverId ($receiverDailyIp), banning all accounts with the same IP..." }

						// Mesmo IP, vamos dar ban em todas as contas do IP atual
						val sameIpDaily = transaction(Databases.loritta) {
							com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq lastReceiverDailyAt[Dailies.ip] and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt to false)
									.toList()
						}

						val receivedByIds = sameIpDaily.map { it[Dailies.receivedById] }

						logger.warn { "Detected IDs: ${receivedByIds.joinToString(", ")}" }

						val reason = "Criar Alt Accounts para farmar sonhos no daily, será que os avisos no website não foram suficientes para você? ¯\\_(ツ)_/¯"

						for (id in receivedByIds) {
							val profile = loritta.getLorittaProfile(id)

							if (profile != null) {
								logger.warn { "Automatically banning $id due to daily abuse..." }
								transaction(Databases.loritta) {
									profile.isBanned = true
									profile.bannedReason = reason
								}
							}
						}

						logger.warn { "Banning ${lastReceiverDailyAt[Dailies.ip]} due to IP abuse, not NAT'd so fuck you." }
						transaction(Databases.loritta) {
							BannedIps.insert {
								it[ip] = lastReceiverDailyAt[Dailies.ip]
								it[bannedAt] = System.currentTimeMillis()
								it[BannedIps.reason] = reason
							}
						}

						// Iremos fakear fingindo que foi um sucesso, mas na verdade foi um ban
						call.respondJson(
								jsonObject(
										"status" to PagarCommand.PayStatus.SUCCESS.toString(),
										"finalMoney" to finalMoney
								)
						)
						return
					}
				}
			}

			transaction(Databases.loritta) {
				giverProfile.money -= howMuch
				receiverProfile.money += finalMoney

				val hasMatchingPayment = SonhosTransaction.select {
					SonhosTransaction.reason eq SonhosPaymentReason.DAILY and
							(SonhosTransaction.quantity eq howMuch.toBigDecimal() ) and
							(SonhosTransaction.receivedBy eq giverId) and
							(SonhosTransaction.givenAt greaterEq System.currentTimeMillis() - Constants.ONE_DAY_IN_MILLISECONDS)
				}.firstOrNull()

				if (taxedMoney != 0.0) {
					SonhosTransaction.insert {
						it[givenBy] = giverProfile.id.value
						it[receivedBy] = null
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = taxedMoney.toBigDecimal()
						it[reason] = SonhosPaymentReason.PAYMENT_TAX
					}
				}

				val transactionId = SonhosTransaction.insertAndGetId {
					it[givenBy] = giverProfile.id.value
					it[receivedBy] = receiverProfile.id.value
					it[givenAt] = System.currentTimeMillis()
					it[quantity] = finalMoney.toBigDecimal()
					it[reason] = SonhosPaymentReason.PAYMENT
				}

				if (hasMatchingPayment != null)
					logger.warn { "Suspicious payment $transactionId by $giverId to $receiverId, sending the same quantity received in the daily" }
			}

			logger.info { "$giverId (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos, recebeu apenas $finalMoney (taxado!))" }

			call.respondJson(
					jsonObject(
							"status" to PagarCommand.PayStatus.SUCCESS.toString(),
							"finalMoney" to finalMoney
					)
			)
		}
	}
}