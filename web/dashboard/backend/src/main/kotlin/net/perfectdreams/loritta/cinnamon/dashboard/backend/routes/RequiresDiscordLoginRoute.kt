package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.lorittaSession
import net.perfectdreams.loritta.cinnamon.dashboard.common.LorittaJsonWebSession

abstract class RequiresDiscordLoginRoute(m: LorittaDashboardBackend, path: String) : LocalizedRoute(m, path) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, userIdentification: LorittaJsonWebSession.UserIdentification)

    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        if (m.config.userAuthenticationOverride.enabled) {
            onAuthenticatedRequest(
                call,
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

            val userIdentification = session.getUserIdentification(true)

            if (userIdentification == null) {
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

            onAuthenticatedRequest(call, userIdentification)
        }
    }
}