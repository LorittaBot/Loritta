package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.mercadopago.PaymentStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class PostMercadoPagoCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/mercadopago") {
	companion object {
		private val logger = KotlinLogging.logger {}
		var allowAnyPayment = false
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
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
				// Verificar se o pagamento foi aprovado
				val payment = com.mrpowergamerbr.loritta.utils.loritta.mercadoPago.getPaymentInfoById(id)
				logger.info { "MercadoPago Payment $id is ${payment.description} - Status: ${payment.status} - Reference ID: ${payment.externalReference}" }

				if (payment.externalReference == null) {
					logger.warn { "MercadoPago Payment $id is ${payment.description} but it is missing the Reference ID!" }
					call.respondJson(jsonObject())
					return
				}

				val internalTransactionId = payment.externalReference.split("-").last()

				val internalPayment = loritta.newSuspendedTransaction {
					Payment.findById(internalTransactionId.toLong())
				}

				if (internalPayment == null) {
					logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) doesn't have a matching internal ID! Bug?" }
					call.respondJson(jsonObject())
					return
				}

				if (payment.status == PaymentStatus.CHARGED_BACK || payment.status == PaymentStatus.IN_MEDIATION) {
					// User charged back the payment, let's ban him!
					logger.warn { "User ${internalPayment.userId} charged back the payment! Let's ban him >:(" }

					val profile = loritta.getLorittaProfileAsync(internalPayment.userId)

					if (profile != null) {
						loritta.newSuspendedTransaction {
							profile.isBanned = true
							profile.bannedReason = "Chargeback/Requesting your money back after a purchase! Why do you pay for something and then chargeback your payment even though you received your product? Payment ID: ${payment.externalReference}"
						}
					}
				} else if (payment.status == PaymentStatus.APPROVED || (com.mrpowergamerbr.loritta.utils.loritta.config.loritta.environment == EnvironmentType.CANARY && allowAnyPayment)) {
					if (internalPayment.paidAt != null) {
						logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) is already paid! Ignoring..." }
						call.respondJson(jsonObject())
						return
					}

					logger.info { "Setting Payment $internalTransactionId as paid! (via MercadoPago payment $id) - Payment made by ${internalPayment.userId}" }

					loritta.newSuspendedTransaction {
						// Pagamento aprovado!
						internalPayment.paidAt = System.currentTimeMillis()
					}

					if (payment.externalReference.startsWith("LORI-BUNDLE-")) {
						// LORI-BUNDLE-InternalTransactionId
						val paymentMetadata = internalPayment.metadata

						if (paymentMetadata == null) {
							logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) is a bundle, but it is missing the bundle metadata!" }
							call.respondJson(jsonObject())
							return
						}

						val bundleId = paymentMetadata["bundleId"].long

						val bundle = loritta.newSuspendedTransaction {
							SonhosBundles.select {
								SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
							}.firstOrNull()
						} ?: run {
							logger.warn { "MercadoPago Payment $id with Reference ID: ${payment.externalReference} ($internalTransactionId) is alredy paid! Ignoring..." }
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

					if (payment.externalReference.startsWith("LORI-DONATE-MP-")) {
						// Criação de nova key:
						// LORI-DONATE-MP-InternalTransactionId
						// Renovação de uma key
						// LORI-DONATE-MP-RENEW-KEY-KeyId-InternalTransactionId
						val isKeyRenewal = payment.externalReference.startsWith("LORI-DONATE-MP-RENEW-KEY-")

						loritta.newSuspendedTransaction {
							internalPayment.expiresAt = System.currentTimeMillis() + Constants.DONATION_ACTIVE_MILLIS

							if (internalPayment.reason == PaymentReason.DONATION) {
								if (isKeyRenewal) {
									val donationKeyId = payment.externalReference.split("-").dropLast(1).last()
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
			}
			else -> {}
		}

		call.respondJson(jsonObject())
	}
}