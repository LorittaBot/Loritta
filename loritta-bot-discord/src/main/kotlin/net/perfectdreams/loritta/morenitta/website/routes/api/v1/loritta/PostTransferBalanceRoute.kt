package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.google.gson.JsonParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.PagarCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredPaymentSonhosTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZonedDateTime

class PostTransferBalanceRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/transfer-balance") {
	companion object {
		private val mutex = Mutex()
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		val giverId = json["giverId"].long
		val receiverId = json["receiverId"].long
		val howMuch = json["howMuch"].long

		logger.info { "Initializing transaction between $giverId and $receiverId, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
		mutex.withLock {
			val receiverProfile = loritta.getOrCreateLorittaProfile(receiverId)
			val giverProfile = loritta.getOrCreateLorittaProfile(giverId)

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
			val todayAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
					.toOffsetDateTime()
					.withHour(0)
					.withMinute(0)
					.withSecond(0)
					.toInstant()
					.toEpochMilli()

			val lastReceiverDailyAt = loritta.newSuspendedTransaction {
				Dailies.select { Dailies.receivedById eq receiverId and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt, SortOrder.DESC)
						.firstOrNull()
			}

			val lastGiverDailyAt = loritta.newSuspendedTransaction {
				Dailies.select { Dailies.receivedById eq giverId and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt, SortOrder.DESC)
						.firstOrNull()
			}

			val beforeGiver = giverProfile.money
			val beforeReceiver = receiverProfile.money

			if (lastReceiverDailyAt != null && lastGiverDailyAt != null) {
				val receiverDailyIp = lastReceiverDailyAt[Dailies.ip]
				val giverDailyIp = lastGiverDailyAt[Dailies.ip]

				if (receiverDailyIp == giverDailyIp) {
					logger.warn { "Same IP detected for $receiverId and $giverId ($receiverDailyIp), you should take a look into it..." }

					// Mesmo IP, vamos dar ban em todas as contas do IP atual
					val sameIpDaily = loritta.newSuspendedTransaction {
						Dailies.select { Dailies.ip eq lastReceiverDailyAt[Dailies.ip] and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt, SortOrder.DESC)
								.toList()
					}

					val receivedByIds = sameIpDaily.map { it[Dailies.receivedById] }

					logger.warn { "Detected IDs: ${receivedByIds.joinToString(", ")}" }
				}
			}

			loritta.newSuspendedTransaction {
				val now = Instant.now()

				giverProfile.takeSonhosNested(howMuch)
				receiverProfile.addSonhosNested(howMuch)

				PaymentUtils.addToTransactionLogNested(
						howMuch,
						SonhosPaymentReason.PAYMENT,
						givenBy = giverProfile.id.value,
						receivedBy = receiverProfile.id.value
				)

				// Cinnamon transaction system
				val paymentResult = PaymentSonhosTransactionResults.insertAndGetId {
					it[PaymentSonhosTransactionResults.givenBy] = giverProfile.id.value
					it[PaymentSonhosTransactionResults.receivedBy] = receiverProfile.id.value
					it[PaymentSonhosTransactionResults.sonhos] = howMuch
					it[PaymentSonhosTransactionResults.timestamp] = now
				}

				SimpleSonhosTransactionsLogUtils.insert(
					receiverProfile.id.value,
					now,
					TransactionType.PAYMENT,
					howMuch,
					StoredPaymentSonhosTransaction(
						giverProfile.id.value,
						receiverProfile.id.value,
						paymentResult.value
					)
				)

				SimpleSonhosTransactionsLogUtils.insert(
					giverProfile.id.value,
					now,
					TransactionType.PAYMENT,
					howMuch,
					StoredPaymentSonhosTransaction(
						giverProfile.id.value,
						receiverProfile.id.value,
						paymentResult.value
					)
				)
			}

			logger.info { "$giverId (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos, recebeu apenas $howMuch (taxado!))" }

			call.respondJson(
					jsonObject(
							"status" to PagarCommand.PayStatus.SUCCESS.toString(),
							"finalMoney" to howMuch
					)
			)
		}
	}
}