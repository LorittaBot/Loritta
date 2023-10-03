package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.config.TrackedTwitchAccount
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

class AddNewGuildTwitchChannelViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel,
    private val userId: Long
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.AddNewGuildTwitchChannelResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized ${this::class.simpleName}")
        fetchConfig()
    }

    fun fetchConfig() {
        fetchConfigAndUpdate(m, this, guildViewModel, _configResource, DashGuildScopedRequest.AddNewGuildTwitchChannelRequest(userId)) {
            it.guild
        }
    }

    companion object {
        fun toMutableConfig(config: TrackedTwitchAccount) = MutableTrackedTwitchAccount(config)
        fun toDataConfig(config: MutableTrackedTwitchAccount) = TrackedTwitchAccount(config.id, config.userId, config.channelId, config.message)
    }

    class MutableTrackedTwitchAccount(config: TrackedTwitchAccount) {
        val id = config.id
        var userId = config.twitchUserId
        var channelId by mutableStateOf(config.channelId)
        var message by mutableStateOf(config.message)
    }
}