package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GuildScopedResponseException
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

/**
 * Fetches a config via RPC using the [request] and automatically updates the [ViewModel] and [GuildViewModel] with the new request.
 */
inline fun <reified ResponseType> fetchConfigAndUpdate(
    m: LorittaDashboardFrontend,
    viewModel: ViewModel,
    guildViewModel: GuildViewModel,
    configResource: MutableState<Resource<ResponseType>>,
    request: DashGuildScopedRequest,
    crossinline guildFromResponse: (ResponseType) -> (DiscordGuild)
) {
    viewModel.scope.launch {
        val response = m.makeRPCRequest<LorittaDashboardRPCResponse>(
            LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                guildViewModel.guildId,
                request
            )
        )

        if (response is LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse) {
            when (val dashResponse = response.dashResponse) {
                is ResponseType -> {
                    guildViewModel._guildInfoResource.value = Resource.Success(guildFromResponse.invoke(dashResponse))
                    configResource.value = Resource.Success(dashResponse)
                }
                DashGuildScopedResponse.InvalidDiscordAuthorization -> {
                    configResource.value = Resource.Failure(GuildScopedResponseException(GuildScopedResponseException.GuildScopedErrorType.InvalidDiscordAuthorization))
                }
                DashGuildScopedResponse.MissingPermission -> {
                    configResource.value = Resource.Failure(GuildScopedResponseException(GuildScopedResponseException.GuildScopedErrorType.MissingPermission))
                }
                DashGuildScopedResponse.UnknownGuild -> {
                    configResource.value = Resource.Failure(GuildScopedResponseException(GuildScopedResponseException.GuildScopedErrorType.UnknownGuild))
                }
                DashGuildScopedResponse.UnknownMember -> {
                    configResource.value = Resource.Failure(GuildScopedResponseException(GuildScopedResponseException.GuildScopedErrorType.UnknownMember))
                }

                else -> error("Unexpected response! ${dashResponse::class.simpleName}")
            }
        } else {
            error("Whoops, ${response::class.simpleName}")
        }
    }
}