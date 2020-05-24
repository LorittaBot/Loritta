package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveText
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class PostPicPayCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/picpay") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val sellerTokenHeader = call.request.header("x-seller-token")

		if (sellerTokenHeader == null || loritta.config.picPay.sellerToken != sellerTokenHeader) {
			logger.warn { "Request Seller Token is different than what it is expected or null! Received Seller Token: $sellerTokenHeader"}
			call.respondJson(jsonObject(), status = HttpStatusCode.Forbidden)
			return
		}

		val body = call.receiveText()
		val json = JsonParser.parseString(body).obj

		val referenceId = json["referenceId"].string
		val authorizationId = json["authorizationId"].nullString

		logger.info { "Received PicPay callback: Reference ID: $referenceId; Authorization ID: $authorizationId" }

		val httpResponse = loritta.http.get<HttpResponse>("https://appws.picpay.com/ecommerce/public/payments/$referenceId/status") {
			header("x-picpay-token", loritta.config.picPay.picPayToken)
		}

		val payloadAsString = httpResponse.readText()

		if (httpResponse.status.value != 200) {
			logger.warn { "Weird status code while checking for PicPay's payment info: ${httpResponse.status.value}; Payload: $payloadAsString" }
			call.respondJson(jsonObject())
			return
		}

		val payload = JsonParser.parseString(payloadAsString)
				.obj

		val status = payload["status"].string

		logger.info { "PicPay payment $referenceId status is $status" }

		if (status == "paid" || status == "complete") {
			val internalTransactionId = referenceId.split("-").last()

			val internalPayment = loritta.newSuspendedTransaction {
				Payment.findById(internalTransactionId.toLong())
			}

			if (internalPayment == null) {
				logger.warn { "PicPay Payment with Reference ID: $referenceId ($internalTransactionId) doesn't have a matching internal ID! Bug?" }
				call.respondJson(jsonObject())
				return
			}

			if (internalPayment.paidAt != null) {
				logger.warn { "PicPay Payment with Reference ID: $referenceId ($internalTransactionId) is already paid! Ignoring..." }
				call.respondJson(jsonObject())
				return
			}

			logger.info { "Setting Payment $internalTransactionId as paid! (via PicPay payment $referenceId) - Payment made by ${internalPayment.userId}" }

			loritta.newSuspendedTransaction {
				// Pagamento aprovado!
				internalPayment.paidAt = System.currentTimeMillis()
			}

			if (referenceId.startsWith("LORI-BUNDLE-")) {
				// LORI-BUNDLE-InternalTransactionId
				val paymentMetadata = internalPayment.metadata

				if (paymentMetadata == null) {
					logger.warn { "PicPay Payment with Reference ID: $referenceId ($internalTransactionId) is a bundle, but it is missing the bundle metadata!" }
					call.respondJson(jsonObject())
					return
				}

				val bundleId = paymentMetadata["bundleId"].long

				val bundle = loritta.newSuspendedTransaction {
					SonhosBundles.select {
						SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
					}.firstOrNull()
				} ?: run {
					logger.warn { "PicPay Payment with Reference ID: $referenceId ($internalTransactionId) is already paid! Ignoring..." }
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
			}

			if (referenceId.startsWith("LORI-DONATE-PP-")) {
				// Criação de nova key:
				// LORI-DONATE-MP-InternalTransactionId
				// Renovação de uma key
				// LORI-DONATE-MP-RENEW-KEY-KeyId-InternalTransactionId
				val isKeyRenewal = referenceId.startsWith("LORI-DONATE-PP-RENEW-KEY-")

				loritta.newSuspendedTransaction {
					internalPayment.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS

					if (internalPayment.reason == PaymentReason.DONATION) {
						if (isKeyRenewal) {
							val donationKeyId = referenceId.split("-").dropLast(1).last()
							logger.info { "Renewing key $donationKeyId with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }
							val donationKey = DonationKey.findById(donationKeyId.toLong())

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
		}

		call.respondJson(jsonObject())
	}
}