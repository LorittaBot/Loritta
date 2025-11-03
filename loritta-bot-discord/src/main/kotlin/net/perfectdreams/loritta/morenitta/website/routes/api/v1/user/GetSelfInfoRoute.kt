package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.networkbans.ApplyBansTask
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.serializable.BackgroundWithVariations
import net.perfectdreams.loritta.serializable.ProfileSectionsResponse
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

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
        val userIdentification = session.getUserIdentification(loritta)

        val profile = loritta.getLorittaProfile(session.userId)

        if (profile != null) {
            loritta.newSuspendedTransaction {
                profile.settings.discordAccountFlags = userIdentification.flags
                profile.settings.discordPremiumType = userIdentification.premiumType
            }
        }

        call.respondJson(
            Json.encodeToString(
                UserIdentification.serializer(),
                UserIdentification(
                    session.userId,
                    session.username,
                    session.discriminator,
                    session.avatarId,
                    false,
                    false,
                    "pt-br",
                    true,
                    "",
                    userIdentification.flags,
                    userIdentification.premiumType
                )
            )
        )
    }
}