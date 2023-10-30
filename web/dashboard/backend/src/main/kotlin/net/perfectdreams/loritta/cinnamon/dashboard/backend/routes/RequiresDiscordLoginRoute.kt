package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.Constants
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.WebsiteUtils
import net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.temmiewebsession.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresDiscordLoginRoute(m: LorittaDashboardBackend, path: String) : LocalizedRoute(m, path) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    )

    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
            call.respondText(WebsiteUtils.getDiscordCrawlerAuthenticationPage(), ContentType.Text.Html)
            return
        }

        if (m.config.userAuthenticationOverride.enabled) {
            onAuthenticatedRequest(
                call,
                TemmieDiscordAuth("dummy", "dummy", "dummy", "dummy", listOf()),
                LorittaJsonWebSession.UserIdentification(
                    m.config.userAuthenticationOverride.id.toString(),
                    m.config.userAuthenticationOverride.name,
                    m.config.userAuthenticationOverride.discriminator,
                    true,
                    m.config.userAuthenticationOverride.globalName,
                    "me@loritta.website",
                    m.config.userAuthenticationOverride.avatarId,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )
        } else {
            // TODO: Fix and improve
            val session = call.lorittaSession
            val (userIdentification, discordAuth) = session.getUserIdentificationAndDiscordAuth("x", "y", call)

            if (discordAuth == null || userIdentification == null) {
                logger.info { "Clearing any set sessions and redirecting request to unauthorized redirect URL... Json Web Session? $session; Is Discord Auth null? ${discordAuth == null}; Is User Identification null? ${userIdentification == null}" }
                call.sessions.clear<LorittaJsonWebSession>()

                call.respondRedirect(LorittaDiscordOAuth2AuthorizeScopeURL(m.lorittaInfo.clientId, m.config.legacyDashboardUrl.removeSuffix("/") + "/dashboard", call.request.origin.scheme + "://" + call.request.host() + call.request.uri).toString())
                return
            }

            // TODO: Check if user is banned
            /* val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
        val bannedState = profile.getBannedState()

        if (bannedState != null)
            throw WebsiteAPIException(
                HttpStatusCode.Unauthorized,
                WebsiteUtils.createErrorPayload(
                    LoriWebCode.BANNED,
                    "You are Loritta Banned!"
                )
            ) */

            onAuthenticatedRequest(call, discordAuth, userIdentification)
        }
    }
}