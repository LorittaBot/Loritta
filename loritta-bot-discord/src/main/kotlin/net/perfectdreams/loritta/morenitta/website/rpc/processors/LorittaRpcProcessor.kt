package net.perfectdreams.loritta.morenitta.website.rpc.processors

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.UnauthorizedTokenException
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification

interface LorittaRpcProcessor {
    suspend fun getDiscordAccountInformation(loritta: LorittaBot, call: ApplicationCall): DiscordAccountInformationResult {
        val session = loritta.dashboardWebServer.getSession(call)
        val userIdentification = session?.retrieveUserIdentificationOrNullIfUnauthorizedRevokeToken(call)

        if (session == null || userIdentification == null)
            return DiscordAccountInformationResult.InvalidDiscordAuthorization

        val profile = loritta.getOrCreateLorittaProfile(session.userId)
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
            val userIdentification: DiscordOAuth2UserIdentification
        ) : DiscordAccountInformationResult()
    }
}