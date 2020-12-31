package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.*
import java.util.*

class PostPerfectPaymentsCallbackRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/perfect-payments") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val sellerTokenHeader = call.request.header("Authorization")

		if (sellerTokenHeader == null || loritta.config.perfectPayments.notificationToken != sellerTokenHeader) {
			logger.warn { "Request Seller Token is different than what it is expected or null! Received Seller Token: $sellerTokenHeader"}
			call.respondJson(jsonObject(), status = HttpStatusCode.Forbidden)
			return
		}

		val body = call.receiveText()
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

			loritta.newSuspendedTransaction {
				BannedUsers.insert {
					it[BannedUsers.userId] = internalPayment.userId
					it[BannedUsers.bannedAt] = System.currentTimeMillis()
					it[BannedUsers.bannedBy] = null
					it[BannedUsers.valid] = true
					it[BannedUsers.expiresAt] = null
					it[BannedUsers.reason] = "Chargeback/Requesting your money back after a purchase! Why do you pay for something and then chargeback your payment even though you received your product? Payment ID: $referenceId; Gateway: $gateway"
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
					val bundleId = metadataAsObj["bundleId"].long

					val bundle = loritta.newSuspendedTransaction {
						SonhosBundles.select {
							// Before we checked if the bundle was active, but what if we want to add new bundles while taking out old bundles?
							// If someone bought and the bundle was deactivated, when the payment was approved, the user wouldn't receive the bundle!
							// So we don't care if the bundle is not active anymore.
							SonhosBundles.id eq bundleId
						}.firstOrNull()
					} ?: run {
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

					val user = lorittaShards.retrieveUserById(internalPayment.userId)
					user?.openPrivateChannel()?.queue {
						it.sendMessage("Seu pagamento foi aprovado com sucesso!").queue()
					}

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

			val user = lorittaShards.retrieveUserById(internalPayment.userId)
			user?.openPrivateChannel()?.queue {
				it.sendMessage("Seu pagamento foi aprovado com sucesso!").queue()
			}
		}

		call.respondJson(jsonObject())
	}
}