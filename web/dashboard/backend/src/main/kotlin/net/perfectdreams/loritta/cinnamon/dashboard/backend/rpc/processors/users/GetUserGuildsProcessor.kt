package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.users

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GetUserGuildsProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.GetUserGuildsRequest, LorittaDashboardRPCResponse.GetUserGuildsResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaDashboardRPCRequest.GetUserGuildsRequest
    ): LorittaDashboardRPCResponse.GetUserGuildsResponse {
        when (val accountInformationResult = getDiscordAccountInformation(m, call)) {
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> TODO()
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> TODO()
            is LorittaDashboardRpcProcessor.DiscordAccountInformationResult.Success -> {
                val userGuilds = accountInformationResult.discordAuth.getUserGuilds()

                return LorittaDashboardRPCResponse.GetUserGuildsResponse.Success(
                    userGuilds.map {
                        LorittaDashboardRPCResponse.GetUserGuildsResponse.DiscordGuild(
                            it.id.toLong(),
                            it.name,
                            it.icon,
                            it.owner,
                            it.permissions,
                            it.features
                        )
                    }
                )
            }
        }
    }
}