package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.BaseRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresAPIDiscordLoginRoute(val m: LorittaDashboardBackend, path: String) : BaseRoute(path) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    )

    override suspend fun onRequest(call: ApplicationCall) {
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
            val webSession = LorittaWebSession(m, session)
            val discordAuth = webSession.getDiscordAuthFromJson()
            val userIdentification = LorittaWebSession(m, session).getUserIdentification(call, true)

            if (discordAuth == null || userIdentification == null) {
                call.respondText("", status = HttpStatusCode.Unauthorized)
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