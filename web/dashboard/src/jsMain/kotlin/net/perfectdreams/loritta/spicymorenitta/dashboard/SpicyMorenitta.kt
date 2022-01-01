
import io.ktor.client.*
import kotlinx.browser.document
import kotlinx.dom.addClass
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash.UserOverview
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.AppState
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.RoutingManager
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLDivElement

val http = HttpClient {}

class SpicyMorenitta {
    val routingManager = RoutingManager(this)
    val appState = AppState(this)
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }

    fun start() {
        routingManager.switchToUserOverview()
        appState.loadData()

        renderComposable(rootElementId = "root") {
            val sessionToken = appState.sessionToken
            val language = appState.i18nContext
            if (sessionToken is State.Success && language is State.Success) {
                // Fade out the single page application loading wrapper...
                spaLoadingWrapper?.addClass("loaded")

                when (val screen = routingManager.screenState) {
                    is Screen.UserOverview -> UserOverview(this@SpicyMorenitta, screen)
                    Screen.Test -> TODO()
                }
            }
        }
    }
}