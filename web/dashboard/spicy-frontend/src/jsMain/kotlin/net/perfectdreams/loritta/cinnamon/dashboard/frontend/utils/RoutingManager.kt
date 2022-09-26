package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.i18n.I18nKeysData

class RoutingManager(private val m: LorittaDashboardFrontend) {
    companion object {
        private val logger = KotlinLogging.loggerClassName(RoutingManager::class)
    }

    var screenState by mutableStateOf<Screen?>(null)

    fun switchBasedOnPath(i18nContext: I18nContext, path: String, backInHistory: Boolean) {
        logger.info { "Trying to find a screen that matches $path" }

        val screenPath = ScreenPath.all.firstOrNull { it.matches(path) }
        if (screenPath != null)
            switch(i18nContext, screenPath.createScreen(m, path), backInHistory)
    }

    fun switch(i18nContext: I18nContext, screen: Screen, backInHistory: Boolean) {
        logger.info { "Switching to screen ${screen::class.simpleName}... Are we going back in history? $backInHistory" }

        val currentScreenState = screenState
        logger.info { "Disposing current screen $currentScreenState" }

        // Automatically dispose the current screen
        currentScreenState?.dispose()

        logger.info { "Switching to new screen..." }
        screenState = screen
        logger.info { "Loading new screen..." }
        screen.onLoad()
        m.globalState.isSidebarOpen = false // Close sidebar if it is open

        // popstate is fired if "data" is different
        // Title is unused
        // https://developer.mozilla.org/en-US/docs/Web/API/History/pushState
        logger.info { "Updating document title to match new screen and pushing new state (if $backInHistory is true)" }
        document.title = "${i18nContext.get(screen.createTitle())} • ${i18nContext.get(I18nKeysData.Website.Dashboard.Title)}"
        val newPath = "/${i18nContext.get(I18nKeysData.Website.Dashboard.LocalePathId)}${screen.createPath().build()}"

        // We don't want to push state when the user is going back their history, because if we did, that would create a "infinite loop" that the browser pops the old state... then we insert the state again
        if (!backInHistory)
            window.history.pushState(newPath, "", newPath)
        // gtagSafe("set", "page_path", newPath)
        // gtagSafe("event", "page_view")
    }
}