package net.perfectdreams.loritta.morenitta.website.rpc.processors

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

interface LorittaRpcProcessor {
    suspend fun getDiscordAccountInformation(loritta: LorittaBot, call: ApplicationCall): DiscordAccountInformationResult {
        val session = call.lorittaSession

        val discordAuth = session.getDiscordAuthFromJson(loritta)
        val userIdentification = session.getUserIdentification(loritta, call)

        if (discordAuth == null || userIdentification == null)
            return DiscordAccountInformationResult.InvalidDiscordAuthorization

        val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
        val bannedState = profile.getBannedState(loritta)

        if (bannedState != null)
            return DiscordAccountInformationResult.UserIsLorittaBanned

        return DiscordAccountInformationResult.Success(discordAuth, userIdentification)
    }

    sealed class DiscordAccountInformationResult {
        object InvalidDiscordAuthorization : DiscordAccountInformationResult()
        object UserIsLorittaBanned : DiscordAccountInformationResult()

        data class Success(
            val discordAuth: TemmieDiscordAuth,
            val userIdentification: LorittaJsonWebSession.UserIdentification
        ) : DiscordAccountInformationResult()
    }
}