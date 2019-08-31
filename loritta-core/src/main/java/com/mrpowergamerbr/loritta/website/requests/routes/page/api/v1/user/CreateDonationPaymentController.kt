package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.*
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.mercadopago.dsl.paymentSettings
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.Local
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/user/donate")
class CreateDonationPaymentController {
    @POST
    @LoriDoNotLocaleRedirect(true)
    @LoriRequiresVariables(true)
    @LoriForceReauthentication(true)
    fun mercadoPago(req: Request, res: Response, @Local variables: MutableMap<String, Any?>, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Body rawConfig: String) {
        res.type(MediaType.json)

        val payload = jsonParser.parse(rawConfig).obj

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

                    notificationUrl = "${loritta.instanceConfig.loritta.website.url}api/v1/callbacks/mercadopago?access=${loritta.config.mercadoPago.ipnAccessToken}"
                }

                val payment = loritta.mercadoPago.createPayment(settings)

                res.send(gson.toJson(jsonObject("redirectUrl" to payment.initPoint)))
            }
            else -> {
                throw WebsiteAPIException(Status.FORBIDDEN,
                        WebsiteUtils.createErrorPayload(
                                LoriWebCode.FORBIDDEN,
                                "Unsupported!"
                        )
                )
            }
        }
    }
}