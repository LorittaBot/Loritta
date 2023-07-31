package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GuildViewModel(m: LorittaDashboardFrontend, scope: CoroutineScope, val guildId: Long) : ViewModel(m, scope) {
    private val _guildInfoResource = mutableStateOf<Resource<LorittaDashboardRPCResponse.GetGuildInfoResponse>>(Resource.Loading())
    val guildInfoResource by _guildInfoResource

    init {
        println("Initialized GuildViewModel")
        fetchGuildInfo()
    }

    private fun fetchGuildInfo() {
        scope.launch {
            m.makeRPCRequestAndUpdateState<LorittaDashboardRPCResponse.GetGuildInfoResponse>(
                _guildInfoResource,
                LorittaDashboardRPCRequest.GetGuildInfoRequest(guildId)
            )
        }
    }
}