package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.TemmieDiscordAuth
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.lorittaSession

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
        if (m.config.userAuthenticationOverride.enabled) {
            onAuthenticatedRequest(
                call,
                TemmieDiscordAuth("dummy", "dummy", "dummy", "dummy", listOf()),
                LorittaJsonWebSession.UserIdentification(
                    m.config.userAuthenticationOverride.id.toString(),
                    m.config.userAuthenticationOverride.name,
                    m.config.userAuthenticationOverride.discriminator,
                    true,
                    "me@loritta.website",
                    m.config.userAuthenticationOverride.avatarId,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )
        } else {
            // TODO: Fix and improve
            val session = call.lorittaSession
            val webSession = LorittaWebSession(m, session)
            val discordAuth = webSession.getDiscordAuthFromJson()
            val userIdentification = LorittaWebSession(m, session).getUserIdentification(call, true)

            if (discordAuth == null || userIdentification == null) {
                logger.info { "Clearing any set sessions and redirecting request to unauthorized redirect URL... Json Web Session? $session; Is Discord Auth null? ${discordAuth == null}; Is User Identification null? ${userIdentification == null}" }
                call.sessions.clear<LorittaJsonWebSession>()
                call.respondRedirect(m.config.unauthorizedRedirectUrl)
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