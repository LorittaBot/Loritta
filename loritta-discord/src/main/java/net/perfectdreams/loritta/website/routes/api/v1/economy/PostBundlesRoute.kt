package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
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
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.mercadopago.dsl.paymentSettings
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PostBundlesRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/bundles/{bundleType}/{bundleId}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = jsonParser.parse(call.receiveText()).obj

		val gateway = payload["gateway"].string
		val bundleId = payload["id"].long

		val bundle = transaction(Databases.loritta) {
			SonhosBundles.select {
				SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
			}.firstOrNull()
		}

		val paymentGateway = PaymentGateway.valueOf(gateway)

		var redirectUrl: String? = null

		val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

		if (bundle != null) {
			val grana = bundle[SonhosBundles.price]
			val sonhos = bundle[SonhosBundles.sonhos]

			val internalPayment = transaction(Databases.loritta) {
				DonationKey.find {
					DonationKeys.expiresAt greaterEq System.currentTimeMillis()
				}

				Payment.new {
					this.userId = userIdentification.id.toLong()
					this.gateway = paymentGateway
					this.reason = PaymentReason.SONHOS_BUNDLE

					this.money = grana.toBigDecimal()
					this.createdAt = System.currentTimeMillis()
					this.metadata = jsonObject(
							"bundleId" to bundleId,
							"bundleType" to "dreams"
					)
				}
			}

			when (paymentGateway) {
				PaymentGateway.MERCADOPAGO -> {
					val settings = paymentSettings {
						item {
							title = "$sonhos sonhos - $whoDonated"
							quantity = 1
							currencyId = "BRL"

							unitPrice = grana.toFloat()
						}

						if (userIdentification.email != null) {
							payer {
								email = userIdentification.email
							}
						}

						externalReference = "LORI-BUNDLE-MP-${internalPayment.id.value}"

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

					val referenceId = "LORI-BUNDLE-PP-${internalPayment.id.value}"

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
		} else {
			throw WebsiteAPIException(HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND,
							"Bundle ID $bundleId not found or it is inactive"
					)
			)
		}
	}
}