package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
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
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = jsonParser.parse(call.receiveText()).obj

		val gateway = payload["gateway"].string
		val bundleId = payload["id"].long

		val bundle = transaction(Databases.loritta) {
			SonhosBundles.select {
				SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
			}.firstOrNull()
		}

		if (bundle != null) {
			when (gateway) {
				"MERCADOPAGO" -> {
					val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"
					val grana = bundle[SonhosBundles.price]
					val sonhos = bundle[SonhosBundles.sonhos]

					val internalPayment = transaction(Databases.loritta) {
						DonationKey.find {
							DonationKeys.expiresAt greaterEq System.currentTimeMillis()
						}

						Payment.new {
							this.userId = userIdentification.id.toLong()
							this.gateway = PaymentGateway.MERCADOPAGO
							this.reason = PaymentReason.SONHOS_BUNDLE

							this.money = grana.toBigDecimal()
							this.createdAt = System.currentTimeMillis()
							this.metadata = jsonObject(
									"bundleId" to bundleId,
									"bundleType" to "dreams"
							)
						}
					}

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

					call.respondJson(jsonObject("redirectUrl" to payment.initPoint))
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