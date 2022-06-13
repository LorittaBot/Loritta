package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen

class RoutingManager(private val m: LorittaDashboardFrontend) {
    var screenState by mutableStateOf<Screen?>(null)

    /* fun switchToHomeOverview(i18nContext: I18nContext) = switch(Screen.HomeOverview(i18nContext))

    fun switchToFanArtsOverview(
        i18nContext: I18nContext,
        page: Int,
        fanArtSortOrder: FanArtSortOrder,
        tags: List<FanArtTag>
    ) = switch(Screen.FanArtsOverview(i18nContext, page, fanArtSortOrder, tags))

    fun switchToArtistFanArtsOverview(
        i18nContext: I18nContext,
        fanArtArtist: FanArtArtist,
        page: Int,
        fanArtSortOrder: FanArtSortOrder,
        tags: List<FanArtTag>
    ) = switch(Screen.FanArtsArtistOverview(i18nContext, fanArtArtist, page, fanArtSortOrder, tags))

    fun switchToFanArtOverview(i18nContext: I18nContext, fanArtArtist: FanArtArtist, fanArt: FanArt) = switch(Screen.FanArtOverview(i18nContext, fanArtArtist, fanArt))

    fun switch(screen: Screen) {
        val currentScreenState = screenState
        // Automatically dispose the current screen ViewModel if the screen has a ViewModel
        if (currentScreenState is Screen.ScreenWithViewModel)
            currentScreenState.model.dispose()
        screenState = screen
        m.appState.isSidebarOpen = false // Close sidebar if it is open

        val newPath = screen.path
        // popstate is fired if "data" is different
        // Title is unused
        // https://developer.mozilla.org/en-US/docs/Web/API/History/pushState
        document.title = screen.title
        window.history.pushState(newPath, "", newPath)
        gtagSafe("set", "page_path", newPath)
        gtagSafe("event", "page_view")
    } */

    fun switch(screen: Screen) {
        val currentScreenState = screenState
        // Automatically dispose the current screen
        currentScreenState?.dispose()
        screenState = screen
        screen.onLoad()
        // m.appState.isSidebarOpen = false // Close sidebar if it is open

        // val newPath = screen.path
        // popstate is fired if "data" is different
        // Title is unused
        // https://developer.mozilla.org/en-US/docs/Web/API/History/pushState
        // document.title = screen.title
        // window.history.pushState(newPath, "", newPath)
        // gtagSafe("set", "page_path", newPath)
        // gtagSafe("event", "page_view")
    }
}