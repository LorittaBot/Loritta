package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
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
import io.ktor.http.HttpStatusCode

class PostDonationPaymentRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/donate") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = jsonParser.parse(call.receiveText()).obj

		val gateway = payload["gateway"].string

		when (gateway) {
			"MERCADOPAGO" -> {
				val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"
				var grana = payload["money"].double
				val keyId = payload["keyId"].nullLong

				val donationKey = if (keyId != null) {
					transaction(Databases.loritta) {
						val key = DonationKey.findById(keyId)

						if (key?.userId == userIdentification.id.toLong())
							return@transaction key
						else
							return@transaction  null
					}
				} else {
					null
				}

				grana = Math.max(0.99, grana)
				grana = Math.min(1000.0, grana)

				val internalPayment = transaction(Databases.loritta) {
					DonationKey.find {
						DonationKeys.expiresAt greaterEq System.currentTimeMillis()
					}

					Payment.new {
						this.userId = userIdentification.id.toLong()
						this.gateway = PaymentGateway.MERCADOPAGO
						this.reason = PaymentReason.DONATION

						if (donationKey != null) {
							this.discount = 0.2
						}

						this.money = grana.toBigDecimal()
						this.createdAt = System.currentTimeMillis()
					}
				}

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
	}
}