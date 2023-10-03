package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

class EditGuildTwitchChannelViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel,
    private val userId: Long
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.EditGuildTwitchChannelResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized ${this::class.simpleName}")
        fetchConfig()
    }

    fun fetchConfig() {
        fetchConfigAndUpdate(m, this, guildViewModel, _configResource, DashGuildScopedRequest.EditGuildTwitchChannelRequest(userId)) {
            it.guild
        }
    }
}