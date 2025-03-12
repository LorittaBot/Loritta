package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.WebsiteUtils
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.receiveAndDecodeRequest
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PostSonhosBundlesRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.MultiFactorAuthenticationDisabledErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.RedirectToUrlResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.UnknownSonhosBundleErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.UnverifiedAccountErrorResponse
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PostSonhosBundlesRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/economy/bundles/sonhos") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        if (true) {
            // Let's go!!
            val (bundleId) = call.receiveAndDecodeRequest<PostSonhosBundlesRequest>()

            val bundle = m.pudding.transaction {
                SonhosBundles.selectAll().where {
                    SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
                }.firstOrNull()
            }

            val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

            if (bundle != null) {
                val grana = bundle[SonhosBundles.price]
                val sonhos = bundle[SonhosBundles.sonhos]

                val paymentUrl = m.perfectPaymentsClient.createPayment(
                    userIdentification.id.toLong(),
                    "$sonhos sonhos - $whoDonated (${userIdentification.id})",
                    (grana * 100).toLong(),
                    (grana * 100).toLong(),
                    PaymentReason.SONHOS_BUNDLE,
                    "LORITTA-BUNDLE-%d",
                    null,
                    buildJsonObject {
                        put("bundleId", bundleId)
                        put("bundleType", "dreams")
                    }
                )

                call.respondLoritta(RedirectToUrlResponse(paymentUrl))
            }
            return
        }

        val userIdentificationFromDiscord = discordAuth.getUserIdentification()

        when (WebsiteUtils.checkIfAccountHasMFAEnabled(userIdentificationFromDiscord)) {
            WebsiteUtils.VerificationResult.MultiFactorAuthenticationDisabled -> {
                call.respondLoritta(
                    MultiFactorAuthenticationDisabledErrorResponse,
                    status = HttpStatusCode.Forbidden
                )
            }
            WebsiteUtils.VerificationResult.UnverifiedAccount -> {
                call.respondLoritta(
                    UnverifiedAccountErrorResponse,
                    status = HttpStatusCode.Forbidden
                )
            }
            WebsiteUtils.VerificationResult.Success -> {
                // Let's go!!
                val (bundleId) = call.receiveAndDecodeRequest<PostSonhosBundlesRequest>()

                val bundle = m.pudding.transaction {
                    SonhosBundles.selectAll().where {
                        SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
                    }.firstOrNull()
                }

                val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

                if (bundle != null) {
                    val grana = bundle[SonhosBundles.price]
                    val sonhos = bundle[SonhosBundles.sonhos]

                    val paymentUrl = m.perfectPaymentsClient.createPayment(
                        userIdentification.id.toLong(),
                        "$sonhos sonhos - $whoDonated (${userIdentification.id})",
                        (grana * 100).toLong(),
                        (grana * 100).toLong(),
                        PaymentReason.SONHOS_BUNDLE,
                        "LORITTA-BUNDLE-%d",
                        null,
                        buildJsonObject {
                            put("bundleId", bundleId)
                            put("bundleType", "dreams")
                        }
                    )

                    call.respondLoritta(RedirectToUrlResponse(paymentUrl))
                } else {
                    call.respondLoritta(
                        UnknownSonhosBundleErrorResponse,
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }
}