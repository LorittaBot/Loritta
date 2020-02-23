package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.mercadopago.PaymentStatus
import org.jetbrains.exposed.sql.transactions.transaction

class PostMercadoPagoCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/mercadopago") {
	companion object {
		private val logger = KotlinLogging.logger {}
		var allowAnyPayment = false
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val access = call.parameters["access"]
		val id = call.parameters["id"]
		val topic = call.parameters["topic"]

		if (access == null || id == null || topic == null || access != com.mrpowergamerbr.loritta.utils.loritta.config.mercadoPago.ipnAccessToken) {
			call.respondJson(jsonObject(), status = HttpStatusCode.Forbidden)
			return
		}

		logger.info { "Received MercadoPago callback: $id - $topic" }

		when (topic) {
			"payment" -> {
				val payment = com.mrpowergamerbr.loritta.utils.loritta.mercadoPago.getPaymentInfoById(id)
				logger.info { "MercadoPago Payment $id is ${payment.description} - Reference ID: ${payment.externalReference}" }


				if (payment.status == PaymentStatus.APPROVED || (com.mrpowergamerbr.loritta.utils.loritta.config.loritta.environment == EnvironmentType.CANARY && allowAnyPayment)) {
					if (payment.externalReference?.startsWith("LORI-DONATE-MP-") == true) {
						// Criação de nova key:
						// LORI-DONATE-MP-InternalTransactionId
						// Renovação de uma key
						// LORI-DONATE-MP-RENEW-KEY-KeyId-InternalTransactionId

						val isKeyRenewal = payment.externalReference.startsWith("LORI-DONATE-MP-RENEW-KEY-")

						val internalTransactionId = payment.externalReference.split("-").last()

						val internalPayment = transaction(Databases.loritta) {
							Payment.findById(internalTransactionId.toLong())
						}

						if (internalPayment == null) {
							logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) doesn't have a matching internal ID! Bug?" }
							call.respondJson(jsonObject())
							return
						}

						if (internalPayment.paidAt != null) {
							logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) is alredy paid! Ignoring..." }
							call.respondJson(jsonObject())
							return
						}

						logger.info { "Setting Payment $internalTransactionId as paid! (via MercadoPago payment $id) - Payment made by ${internalPayment.userId}" }
						transaction(Databases.loritta) { // Pagamento aprovado
							internalPayment.paidAt = System.currentTimeMillis()
							internalPayment.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS

							if (internalPayment.reason == PaymentReason.DONATION) {
								if (isKeyRenewal) {
									val donationKeyId = payment.externalReference.split("-").dropLast(1).last()
									logger.info { "Renewing key $donationKeyId with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }
									val donationKey = DonationKey.findById(donationKeyId.toLong())

									if (donationKey == null) {
										logger.warn { "Key renewal for key $donationKeyId for ${internalPayment.userId} failed! Key doesn't exist! Bug?" }
										return@transaction
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
			}
			else -> {}
		}

		call.respondJson(jsonObject())
	}
}