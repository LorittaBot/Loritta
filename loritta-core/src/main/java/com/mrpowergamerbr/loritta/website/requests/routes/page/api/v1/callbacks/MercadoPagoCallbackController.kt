package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.mercadopago.PaymentStatus
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/callbacks/mercadopago")
class MercadoPagoCallbackController {
	companion object {
		private val logger = KotlinLogging.logger {}
		var allowAnyPayment = false
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val access = req.param("access").valueOrNull()
		val id = req.param("id").valueOrNull()
		val topic = req.param("topic").valueOrNull()

		if (access == null || id == null || topic == null || access != loritta.config.mercadoPago.ipnAccessToken) {
			res.status(Status.FORBIDDEN)
			res.send("{}")
			return
		}

		logger.info { "Received MercadoPago callback: $id - $topic" }

		when (topic) {
			"payment" -> {
				val payment = loritta.mercadoPago.getPaymentInfoById(id)
				logger.info { "MercadoPago Payment $id is ${payment.description} - Reference ID: ${payment.externalReference}" }


				if (payment.status == PaymentStatus.APPROVED || (loritta.config.loritta.environment == EnvironmentType.CANARY && allowAnyPayment)) {
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
							res.status(Status.OK)
							res.send("{}")
							return
						}

						if (internalPayment.paidAt != null) {
							logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) is alredy paid! Ignoring..." }
							res.status(Status.OK)
							res.send("{}")
							return
						}

						logger.info { "Setting Payment $internalTransactionId as paid! (via MercadoPago payment $id) - Payment made by ${internalPayment.userId}" }
						transaction(Databases.loritta) { // Pagamento aprovado
							internalPayment.paidAt = System.currentTimeMillis()
							internalPayment.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS

							if (internalPayment.reason == PaymentReason.DONATION) {
								if (isKeyRenewal) {
									val donationKeyId = payment.externalReference.split("-").dropLast(1).last()
									MercadoPagoCallbackController.logger.info { "Renewing key $donationKeyId with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }
									val donationKey = DonationKey.findById(donationKeyId.toLong())

									if (donationKey == null) {
										MercadoPagoCallbackController.logger.warn { "Key renewal for key $donationKeyId for ${internalPayment.userId} failed! Key doesn't exist! Bug?" }
										res.status(Status.OK)
										res.send("{}")
										return@transaction
									}

									donationKey.expiresAt += 2_764_800_000 // 32 dias
								} else {
									if (internalPayment.money > 9.99.toBigDecimal()) {
										MercadoPagoCallbackController.logger.info { "Creating donation key with value ${internalPayment.money.toDouble()} for ${internalPayment.userId}" }

										DonationKey.new {
											this.userId = internalPayment.userId
											this.expiresAt = System.currentTimeMillis() + 2_764_800_000 // 32 dias
											this.value = internalPayment.money.toDouble()
										}
									}
								}
							}
						}

						val user = runBlocking { lorittaShards.retrieveUserById(internalPayment.userId) }
						if (user != null) {
							user.openPrivateChannel().queue {
								it.sendMessage("Seu pagamento foi aprovado com sucesso!").queue()
							}
						}
					}
				}
			}
			else -> {}
		}

		res.status(Status.OK)
		res.send("{}")
	}
}