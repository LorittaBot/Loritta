package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.sequins.ktor.BaseRoute

class GetSelfInfoRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/users/@me") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val session = loritta.dashboardWebServer.getSession(call) ?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
            WebsiteUtils.createErrorPayload(
                loritta,
                LoriWebCode.UNAUTHORIZED
            )
        )
 
        val profile = loritta.getLorittaProfile(session.userId)

        if (profile != null) {
            loritta.newSuspendedTransaction {
                profile.settings.discordAccountFlags = session.cachedUserIdentification.flags
                profile.settings.discordPremiumType = session.cachedUserIdentification.premiumType
            }
        }

        call.respondJson(
            Json.encodeToString(
                UserIdentification.serializer(),
                UserIdentification(
                    session.userId,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.discriminator,
                    session.cachedUserIdentification.avatarId,
                    false,
                    session.cachedUserIdentification.mfaEnabled,
                    session.cachedUserIdentification.locale,
                    session.cachedUserIdentification.verified,
                    session.cachedUserIdentification.email,
                    session.cachedUserIdentification.flags,
                    session.cachedUserIdentification.premiumType
                )
            )
        )
    }
}