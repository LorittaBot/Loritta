package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GuildsViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope
) : ViewModel(m, scope) {
    private val _guildsResource = mutableStateOf<Resource<LorittaDashboardRPCResponse.GetUserGuildsResponse>>(Resource.Loading())
    val guildResource by _guildsResource

    init {
        println("Initialized GuildsViewModel")
        fetchGuilds()
    }

    private fun fetchGuilds() {
        scope.launch {
            m.makeRPCRequestAndUpdateState<LorittaDashboardRPCResponse.GetUserGuildsResponse>(
                _guildsResource,
                LorittaDashboardRPCRequest.GetUserGuildsRequest()
            )
        }
    }
}