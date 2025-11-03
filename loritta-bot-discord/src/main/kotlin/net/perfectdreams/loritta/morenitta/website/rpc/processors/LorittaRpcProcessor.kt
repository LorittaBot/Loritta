package net.perfectdreams.loritta.morenitta.website.rpc.processors

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

interface LorittaRpcProcessor {
    suspend fun getDiscordAccountInformation(loritta: LorittaBot, call: ApplicationCall): DiscordAccountInformationResult {
        val session = loritta.dashboardWebServer.getSession(call)
        val userIdentification = session?.getUserIdentification(loritta)

        if (session == null || userIdentification == null)
            return DiscordAccountInformationResult.InvalidDiscordAuthorization

        val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
        val bannedState = profile.getBannedState(loritta)

        if (bannedState != null)
            return DiscordAccountInformationResult.UserIsLorittaBanned

        return DiscordAccountInformationResult.Success(session, userIdentification)
    }

    sealed class DiscordAccountInformationResult {
        object InvalidDiscordAuthorization : DiscordAccountInformationResult()
        object UserIsLorittaBanned : DiscordAccountInformationResult()

        data class Success(
            val session: UserSession,
            val userIdentification: DiscordLoginUserDashboardRoute.UserIdentification
        ) : DiscordAccountInformationResult()
    }
}