package net.perfectdreams.loritta.website.frontend.views

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.routes.ApplicationCommandsRoute
import net.perfectdreams.loritta.website.frontend.routes.BaseRoute
import net.perfectdreams.loritta.website.frontend.routes.HomeRoute
import net.perfectdreams.loritta.website.frontend.routes.LegacyCommandsRoute
import kotlin.math.max

class ViewManager(val showtime: LorittaWebsiteFrontend) {
    var currentView: DokyoView? = null
    var preparingView: DokyoView? = null
    // We need to use a lock to avoid calling "onLoad()" before "onPreLoad()" is finished!
    var preparingMutex = Mutex()
    val routes = mutableListOf<BaseRoute>(
        HomeRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime)
    )

    suspend fun preparePreLoad(path: String) {
        println("Path: $path")
        val matchedRoute = routes.firstOrNull { matches(it.path, path) }
        println("Found route $matchedRoute for $path")
        if (matchedRoute == null) {
            this.preparingView = null
            return
        }

        val preparingView = matchedRoute.onRequest()
        if (preparingView != null) {
            this.preparingView = preparingView
            preparingMutex.withLock {
                preparingView.onPreLoad()
            }
        }
    }

    // Implementação básica do sistema de paths do ktor
    fun matches(path: String, input: String): Boolean {
        val sourceSplit = path.removeSuffix("/").split("/")
        val inputSplit = input.removeSuffix("/").split("/")

        var inputSplitLength = 0

        for (index in 0 until max(sourceSplit.size, inputSplit.size)) {
            val sInput = sourceSplit.getOrNull(index)
            val iInput = inputSplit.getOrNull(index)

            // Check if it is a group match
            if (sInput != null && sInput.startsWith("{") && sInput.endsWith("}")) {
                if (iInput == null && sInput.endsWith("?}")) {
                    inputSplitLength++
                    continue
                }

                inputSplitLength++
                continue
            }

            if (iInput == null)
                return false

            if (iInput != sInput) // Input does not match
                return false

            inputSplitLength++
        }

        return true
    }

    /**
     * Switches the current [preparingView] to the [currentView]
     */
    suspend fun switchPreparingToActiveView() {
        // Now we will wipe all page specific coroutines
        showtime.pageSpecificTasks.forEach { it.cancel() }
        showtime.pageSpecificTasks.clear()

        // Now that all of the elements are loaded, we can call the "onLoad()"
        showtime.viewManager.preparingView?.onLoad()
        showtime.viewManager.currentView = showtime.viewManager.preparingView
    }
}