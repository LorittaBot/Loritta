package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class WelcomerViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.GetGuildWelcomerConfigResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized WelcomerViewModel")
        fetchConfig()
    }

    private fun fetchConfig() {
        scope.launch {
            val response = m.makeRPCRequest<LorittaDashboardRPCResponse>(
                LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                    guildViewModel.guildId,
                    DashGuildScopedRequest.GetGuildWelcomerConfigRequest
                )
            )

            if (response is LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse) {
                when (val dashResponse = response.dashResponse) {
                    is DashGuildScopedResponse.GetGuildWelcomerConfigResponse -> {
                        guildViewModel._guildInfoResource.value = Resource.Success(dashResponse.guild)
                        _configResource.value = Resource.Success(dashResponse)
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

    companion object {
        fun toMutableConfig(config: GuildWelcomerConfig) = MutableGuildWelcomerConfig(config)

        class MutableGuildWelcomerConfig(config: GuildWelcomerConfig) {
            var _tellOnJoin = mutableStateOf(config.tellOnJoin)
            var tellOnJoin by _tellOnJoin
            var _channelJoinId = mutableStateOf(config.channelJoinId)
            var channelJoinId by _channelJoinId
            var _joinMessage = mutableStateOf(config.joinMessage)
            var joinMessage by _joinMessage
            var _deleteJoinMessagesAfter = mutableStateOf(config.deleteJoinMessagesAfter)
            var deleteJoinMessagesAfter by _deleteJoinMessagesAfter

            var _tellOnRemove = mutableStateOf(config.tellOnRemove)
            var tellOnRemove by _tellOnRemove
            var _channelRemoveId = mutableStateOf(config.channelRemoveId)
            var channelRemoveId by _channelRemoveId
            var _removeMessage = mutableStateOf(config.removeMessage)
            var removeMessage by _removeMessage
            var _deleteRemoveMessagesAfter = mutableStateOf(config.deleteRemoveMessagesAfter)
            var deleteRemoveMessagesAfter by _deleteRemoveMessagesAfter

            var _tellOnPrivateJoin = mutableStateOf(config.tellOnPrivateJoin)
            var tellOnPrivateJoin by _tellOnPrivateJoin
            var _joinPrivateMessage = mutableStateOf(config.joinPrivateMessage)
            var joinPrivateMessage by _joinPrivateMessage

            var _tellOnBan = mutableStateOf(config.tellOnBan)
            var tellOnBan by _tellOnBan
            var _bannedMessage = mutableStateOf(config.bannedMessage)
            var bannedMessage by _bannedMessage
        }
    }
}