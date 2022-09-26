package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSonhosBundlesResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class SonhosShopScreen(m: LorittaDashboardFrontend) : Screen(m) {
    var sonhosBundlesState = mutableStateOf<State<GetSonhosBundlesResponse>>(State.Loading())
    var sonhosBundles by sonhosBundlesState

    override fun createPath() = ScreenPath.SonhosShopScreenPath
    override fun createTitle() = I18nKeysData.Website.Dashboard.SonhosShop.Title

    override fun onLoad() {
        launch {
            updateSonhosBundles()
        }
    }

    private suspend fun updateSonhosBundles() {
        m.makeApiRequestAndUpdateState(sonhosBundlesState, HttpMethod.Get, "/api/v1/economy/bundles/sonhos")
    }
}