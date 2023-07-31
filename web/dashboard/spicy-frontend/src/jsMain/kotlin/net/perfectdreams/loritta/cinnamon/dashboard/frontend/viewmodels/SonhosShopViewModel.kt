package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSonhosBundlesResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource

class SonhosShopViewModel(m: LorittaDashboardFrontend, scope: CoroutineScope) : ViewModel(m, scope) {
    var sonhosBundlesResource = mutableStateOf<Resource<GetSonhosBundlesResponse>>(Resource.Loading())
    var sonhosBundles by sonhosBundlesResource

    init {
        println("Initialized SonhosShopViewModel")
        fetchSonhosBundles()
    }

    private fun fetchSonhosBundles() {
        scope.launch {
            m.makeApiRequestAndUpdateState(sonhosBundlesResource, HttpMethod.Get, "/api/v1/economy/bundles/sonhos")
        }
    }
}