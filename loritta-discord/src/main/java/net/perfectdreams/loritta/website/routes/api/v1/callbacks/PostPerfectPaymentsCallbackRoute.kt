package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class PostPerfectPaymentsCallbackRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/perfect-payments") {
	companion object {
		private val logger = KotlinLogging.logger {}

		suspend fun sendPaymentApprovedDirectMessage(userId: Long, locale: BaseLocale, supportUrl: String) {
			val user = lorittaShards.retrieveUserById(userId)
			user?.openPrivateChannel()?.queue {
				val embed = EmbedBuilder()
						.setTitle("${Emotes.LORI_RICH} ${locale["economy.paymentApprovedNotification.title"]}")
						.setDescription(
								locale.getList(
										"economy.paymentApprovedNotification.description",
										supportUrl,
										"${Emotes.LORI_HEART1}${Emotes.LORI_HEART2}",
										Emotes.LORI_NICE,
										Emotes.LORI_SMILE
								).joinToString("\n")
						)
						.setImage("https://cdn.discordapp.com/attachments/513405772911345664/811320940335071263/economy_original.png")
						.setColor(Color(47, 182, 92))
						.setTimestamp(Instant.now())
						.build()

				it.sendMessage(embed).queue()
			}
		}

		private suspend fun retrieveSonhosBundleFromMetadata(loritta: LorittaDiscord, metadata: JsonObject): ResultRow? {
			val metadataAsObj = metadata.obj

			val bundleType = metadataAsObj["bundleType"].nullString

			if (bundleType == "dreams") {
				// LORI-BUNDLE-InternalTransactionId
				val bundleId = metadataAsObj["bundleId"].long

				val bundle = loritta.newSuspendedTransaction {
					SonhosBundles.select {
						// Before we checked if the bundle was active, but what if we want to add new bundles while taking out old bundles?
						// If someone bought and the bundle was deactivated, when the payment was approved, the user wouldn't receive the bundle!
						// So we don't care if the bundle is not active anymore.
						SonhosBundles.id eq bundleId
					}.firstOrNull()
				} ?: return null

				return bundle
			}

			return null
		}
	}

	/**
	 * Stores the chargeback quantity made by the user
	 */
	private val chargebackedQuantity = ConcurrentHashMap<Long, AtomicLong>()

	/**
	 * Stores the current active job of the chargeback process, used for cancellation
	 */
	private val jobs = ConcurrentHashMap<Long, Job>()

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val sellerTokenHeader = call.request.header("Authorization")

		if (sellerTokenHeader == null || loritta.config.perfectPayments.notificationToken != sellerTokenHeader) {
			logger.warn { "Request Seller Token is different than what it is expected or null! Received Seller Token: $sellerTokenHeader"}
			call.respondJson(jsonObject(), status = HttpStatusCode.Forbidden)
			return
		}

		val body = withContext(Dispatchers.IO) { call.receiveText() }
		val json = JsonParser.parseString(body).obj

		val referenceId = UUID.fromString(json["referenceId"].string)
		val gateway = json["gateway"].string
		val status = json["status"].string

		logger.info { "Received PerfectPayments callback: Reference ID: $referenceId; Gateway: $gateway; Status: $status" }

		val internalPayment = loritta.newSuspendedTransaction {
			Payment.find { Payments.referenceId eq referenceId }
					.firstOrNull()
		}

		val internalTransactionId = internalPayment?.id?.value

		if (internalPayment == null) {
			logger.warn { "PerfectPayments Payment with Reference ID: $referenceId ($internalTransactionId) doesn't have a matching internal ID! Bug?" }
			call.respondJson(jsonObject())
			return
		}

		if (status == "CHARGED_BACK") {
			// User charged back the payment, let's ban him!
			logger.warn { "User ${internalPayment.userId} charged back the payment! Let's ban him >:(" }

			val metadata = internalPayment.metadata
			if (metadata != null) {
				if (internalPayment.reason == PaymentReason.SONHOS_BUNDLE) {
					val metadataAsObj = metadata.obj

					val bundleType = metadataAsObj["bundleType"].nullString

					if (bundleType == "dreams") {
						// LORI-BUNDLE-InternalTransactionId
						val bundle = retrieveSonhosBundleFromMetadata(loritta, metadataAsObj)

						if (bundle != null) {
							// If it is a sonhos bundle, we need to remove all the sonhos from the bundle to the user
							// We are going to execute it in a separate task, to avoid hanging the request!
							//
							// However we need to keep in mind that users can buy multiple sonhos packages and then charging back all at once, so we need to have a delay waiting to get all the quantity and then process the sonhos removal.
							jobs[internalPayment.userId]?.cancel()

							// We use a AtomicLong to avoid concurrency issues
							chargebackedQuantity[internalPayment.userId] = chargebackedQuantity.getOrPut(internalPayment.userId) { AtomicLong() }
									.also { it.addAndGet(bundle[SonhosBundles.sonhos]) }

							jobs[internalPayment.userId] = GlobalScope.launch(loritta.coroutineDispatcher) {
								delay(300_000L) // Five minutes

								// And then dispatch to a separate job, just to avoid any other cancellations causing issues after we already started removing the sonhos
								GlobalScope.launch(loritta.coroutineDispatcher) {
									val quantity = chargebackedQuantity[internalPayment.userId]
											?.get()

									chargebackedQuantity.remove(internalPayment.userId)

									if (quantity != null) {
										logger.info { "Starting a chargeback sonhos job for ${internalPayment.userId}, chargeback quantity: $quantity" }

										PaymentUtils.removeSonhosDueToChargeback(
												loritta,
												internalPayment.userId,
												quantity,
												removeSonhos = true,
												notifyChargebackUser = false,
												notifyUsers = true
										)
									} else {
										logger.warn { "Tried starting a chargeback sonhos job for ${internalPayment.userId}, but the chargeback quantity is null!" }
									}
								}
							}
						} else {
							logger.warn { "PerfectPayments Payment with Reference ID: $referenceId ($internalTransactionId) does not have a valid bundle! We are still going to ban the user due to chargeback..." }
						}
					}
				}
			}

			loritta.newSuspendedTransaction {
				BannedUsers.insert {
					it[userId] = internalPayment.userId
					it[bannedAt] = System.currentTimeMillis()
					it[bannedBy] = null
					it[valid] = true
					it[expiresAt] = null
					it[reason] = "Chargeback/Requesting your money back after a purchase! Why do you pay for something and then chargeback your payment even though you received your product? Payment ID: $referenceId; Gateway: $gateway"
				}
			}
		} else if (status == "APPROVED") {
			if (internalPayment.paidAt != null) {
				logger.warn { "PerfectPayments Payment with Reference ID: $referenceId ($internalTransactionId); Gateway: $gateway is already paid! Ignoring..." }
				call.respondJson(jsonObject())
				return
			}

			logger.info { "Setting Payment $internalTransactionId as paid! (via PerfectPayments payment $referenceId; Gateway: $gateway) - Payment made by ${internalPayment.userId}" }

			loritta.newSuspendedTransaction {
				// Pagamento aprovado!
				internalPayment.paidAt = System.currentTimeMillis()
			}

			val metadata = internalPayment.metadata
			if (metadata != null) {
				val metadataAsObj = metadata.obj

				val bundleType = metadataAsObj["bundleType"].nullString

				if (bundleType == "dreams") {
					// LORI-BUNDLE-InternalTransactionId
					val bundle = retrieveSonhosBundleFromMetadata(loritta, metadataAsObj)

					if (bundle == null) {
						logger.warn { "PerfectPayments Payment with Reference ID: $referenceId ($internalTransactionId) does not have a valid bundle!" }
						call.respondJson(jsonObject())
						return
					}

					loritta.newSuspendedTransaction {
						Profiles.update({ Profiles.id eq internalPayment.userId }) {
							with(SqlExpressionBuilder) {
								it.update(money, money + bundle[SonhosBundles.sonhos])
							}
						}
					}

					sendPaymentApprovedDirectMessage(internalPayment.userId, loritta.getLocaleById("default"), "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}support")

					call.respondJson(jsonObject())
					return
				}
			}

			// Criação de nova key:
			// LORI-DONATE-MP-InternalTransactionId
			// Renovação de uma key
			// LORI-DONATE-MP-RENEW-KEY-KeyId-InternalTransactionId
			val isKeyRenewal = metadata != null && metadata.obj["renewKey"].nullLong != null

			loritta.newSuspendedTransaction {
				internalPayment.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS

				if (internalPayment.reason == PaymentReason.DONATION) {
					if (isKeyRenewal) {
						val donationKeyId = metadata!!.obj["renewKey"].long
						logger.info { "Renewing key $donationKeyId with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }
						val donationKey = DonationKey.findById(donationKeyId)

						if (donationKey == null) {
							logger.warn { "Key renewal for key $donationKeyId for ${internalPayment.userId} failed! Key doesn't exist! Bug?" }
							return@newSuspendedTransaction
						}

						donationKey.expiresAt += 2_764_800_000 // 32 dias
					} else {
						if (internalPayment.money > 9.99.toBigDecimal()) {
							logger.info { "Creating donation key with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }

							DonationKey.new {
								this.userId = internalPayment.userId
								this.expiresAt = System.currentTimeMillis() + 2_764_800_000 // 32 dias
								this.value = internalPayment.money.toDouble()
							}
						}
					}
				}
			}

			sendPaymentApprovedDirectMessage(internalPayment.userId, loritta.getLocaleById("default"), "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}support")
		}

		call.respondJson(jsonObject())
	}
}