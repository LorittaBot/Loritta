package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GamerSaferVerifyViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    init {
        println("Initialized GamerSaferVerifyViewModel")
        fetchConfig()
    }

    private fun fetchConfig() {
        scope.launch {
            val response = m.makeRPCRequest<LorittaDashboardRPCResponse>(
                LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                    guildViewModel.guildId,
                    DashGuildScopedRequest.GetGuildInfoRequest
                )
            )

            if (response is LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse) {
                when (val dashResponse = response.dashResponse) {
                    is DashGuildScopedResponse.GetGuildInfoResponse -> {
                        guildViewModel._guildInfoResource.value = Resource.Success(dashResponse.guild)
                    }
                    DashGuildScopedResponse.InvalidDiscordAuthorization -> TODO()
                    DashGuildScopedResponse.MissingPermission -> TODO()
                    DashGuildScopedResponse.UnknownGuild -> TODO()
                    DashGuildScopedResponse.UnknownMember -> TODO()

                    else -> error("Unexpected response! ${dashResponse::class.simpleName}")
                }
            } else {
                error("Whoops, ${response::class.simpleName}")
            }
        }
    }
}