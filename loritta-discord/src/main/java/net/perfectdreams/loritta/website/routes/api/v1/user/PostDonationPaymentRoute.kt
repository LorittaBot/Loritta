package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.request.receiveText
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.mercadopago.dsl.paymentSettings
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.transaction

class PostDonationPaymentRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/donate") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = jsonParser.parse(call.receiveText()).obj

		val gateway = payload["gateway"].string

		var redirectUrl: String? = null

		val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

		var grana = payload["money"].double
		val keyId = payload["keyId"].nullLong

		val donationKey = if (keyId != null) {
			loritta.newSuspendedTransaction {
				val key = DonationKey.findById(keyId)

				if (key?.userId == userIdentification.id.toLong())
					return@newSuspendedTransaction key
				else
					return@newSuspendedTransaction  null
			}
		} else {
			null
		}

		grana = Math.max(0.99, grana)
		grana = Math.min(1000.0, grana)

		val paymentGateway = PaymentGateway.valueOf(gateway)

		val internalPayment = loritta.newSuspendedTransaction {
			DonationKey.find {
				DonationKeys.expiresAt greaterEq System.currentTimeMillis()
			}

			Payment.new {
				this.userId = userIdentification.id.toLong()
				this.gateway = paymentGateway
				this.reason = PaymentReason.DONATION

				if (donationKey != null) {
					this.discount = 0.2
				}

				this.money = grana.toBigDecimal()
				this.createdAt = System.currentTimeMillis()
			}
		}

		when (paymentGateway) {
			PaymentGateway.MERCADOPAGO -> {
				val settings = paymentSettings {
					item {
						title = "Doação para a Loritta - $whoDonated"
						quantity = 1
						currencyId = "BRL"

						unitPrice = if (donationKey != null) {
							(donationKey.value * 0.8).toFloat()
						} else {
							grana.toFloat()
						}
					}

					if (userIdentification.email != null) {
						payer {
							email = userIdentification.email
						}
					}

					if (donationKey != null) {
						externalReference = "LORI-DONATE-MP-RENEW-KEY-${donationKey.id.value}-${internalPayment.id.value}"
					} else {
						externalReference = "LORI-DONATE-MP-${internalPayment.id.value}"
					}

					notificationUrl = "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}api/v1/callbacks/mercadopago?access=${com.mrpowergamerbr.loritta.utils.loritta.config.mercadoPago.ipnAccessToken}"
				}

				val payment = com.mrpowergamerbr.loritta.utils.loritta.mercadoPago.createPayment(settings)

				redirectUrl = payment.initPoint
			}
			PaymentGateway.PICPAY -> {
				val firstName = payload["firstName"].string
				val lastName = payload["lastName"].string
				val document = payload["document"].string
				val email = payload["email"].string
				val phone = payload["phone"].string

				val referenceId = if (donationKey != null) {
					"LORI-DONATE-PP-RENEW-KEY-${donationKey.id.value}-${internalPayment.id.value}"
				} else {
					"LORI-DONATE-PP-${internalPayment.id.value}"
				}

				val httpResponse = loritta.http.post<HttpResponse>("https://appws.picpay.com/ecommerce/public/payments") {
					contentType(ContentType.Application.Json)
					header("x-picpay-token", loritta.config.picPay.picPayToken)

					body = jsonObject(
							"referenceId" to referenceId,
							"callbackUrl" to "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}api/v1/callbacks/picpay",
							"value" to grana,
							"buyer" to jsonObject(
									"firstName" to firstName,
									"lastName" to lastName,
									"document" to document,
									"email" to email,
									"phone" to phone
							)
					).toString()
				}

				val payload = httpResponse.readText()

				logger.debug { "PicPay response: $payload" }

				val json = JsonParser.parseString(payload).obj

				redirectUrl = json["paymentUrl"].string
			}
			else -> {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN,
								"Unsupported!"
						)
				)
			}
		}

		call.respondJson(jsonObject("redirectUrl" to redirectUrl))
	}
}