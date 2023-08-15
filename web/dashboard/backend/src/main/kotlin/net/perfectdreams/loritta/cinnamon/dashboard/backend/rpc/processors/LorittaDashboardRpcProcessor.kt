package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors

import io.ktor.server.application.*
import io.ktor.server.request.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.lorittaSession
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

interface LorittaDashboardRpcProcessor<Req: LorittaDashboardRPCRequest, Res: LorittaDashboardRPCResponse> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(call: ApplicationCall, request: Req): Res

    suspend fun getDiscordAccountInformation(loritta: LorittaDashboardBackend, call: ApplicationCall): DiscordAccountInformationResult {
        if (loritta.config.userAuthenticationOverride.enabled) {
            return DiscordAccountInformationResult.Success(
                TemmieDiscordAuth("dummy", "dummy", "dummy", "dummy", listOf()),
                LorittaJsonWebSession.UserIdentification(
                    loritta.config.userAuthenticationOverride.id.toString(),
                    loritta.config.userAuthenticationOverride.name,
                    loritta.config.userAuthenticationOverride.discriminator,
                    true,
                    loritta.config.userAuthenticationOverride.globalName,
                    "me@loritta.website",
                    loritta.config.userAuthenticationOverride.avatarId,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )
        }

        try {
            val session = call.lorittaSession
            val webSession = LorittaWebSession(loritta, session)

            val discordAuth = webSession.getDiscordAuthFromJson()
            val userIdentification = webSession.getUserIdentification(call)

            if (discordAuth == null || userIdentification == null)
                return DiscordAccountInformationResult.InvalidDiscordAuthorization

            // TODO: Check if user is banned
            /* val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
        val bannedState = profile.getBannedState(loritta)

        if (bannedState != null)
            return DiscordAccountInformationResult.UserIsLorittaBanned */

            return DiscordAccountInformationResult.Success(discordAuth, userIdentification)
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to query the user's Discord Auth data! Returning InvalidDiscordAuthorization..." }
            return DiscordAccountInformationResult.InvalidDiscordAuthorization
        }
    }

    sealed class DiscordAccountInformationResult {
        object InvalidDiscordAuthorization : DiscordAccountInformationResult()
        object UserIsLorittaBanned : DiscordAccountInformationResult()

        data class Success(
            val discordAuth: TemmieDiscordAuth,
            val userIdentification: LorittaJsonWebSession.UserIdentification
        ) : DiscordAccountInformationResult()
    }

    suspend fun validateDashboardToken(loritta: LorittaDashboardBackend, call: ApplicationCall): DashboardTokenResult {
        val authorizationToken = call.request.header("Authorization") ?: return DashboardTokenResult.InvalidTokenAuthorization

        val valid = loritta.config.authorizationTokens.any { it.token == authorizationToken }

        return if (valid) {
            DashboardTokenResult.Success
        } else {
            DashboardTokenResult.InvalidTokenAuthorization
        }
    }

    sealed class DashboardTokenResult {
        object InvalidTokenAuthorization : DashboardTokenResult()

        object Success : DashboardTokenResult()
    }
}