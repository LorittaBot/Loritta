package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import io.ktor.client.*
import kotlinx.browser.document
import kotlinx.dom.addClass
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.TestScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.RoutingManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLDivElement

class LorittaDashboardFrontend {
    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)
    val http = HttpClient {
        expectSuccess = false
    }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }

    fun start() {
        globalState.launch { globalState.updateSelfUserInfo() }
        globalState.launch { globalState.updateI18nContext() }

        routingManager.switch(ShipEffectsScreen(this))

        document.addEventListener("DOMContentLoaded", {
            renderComposable(rootElementId = "root") {
                val userInfo = globalState.userInfo
                val i18nContext = globalState.i18nContext

                if (userInfo !is State.Success || i18nContext !is State.Success) {
                    Text("Loading...")
                } else {
                    // Fade out the single page application loading wrapper...
                    spaLoadingWrapper?.addClass("loaded")

                    Div(attrs = { id("wrapper") }) {
                        when (val screen = routingManager.screenState) {
                            is ShipEffectsScreen -> {
                                ShipEffectsOverview(this@LorittaDashboardFrontend, screen, i18nContext.value)
                            }
                            is TestScreen -> {
                                Button(attrs = {
                                    onClick {
                                        routingManager.switch(ShipEffectsScreen(this@LorittaDashboardFrontend))
                                    }
                                }) {
                                    Text("OwO")
                                }
                            }
                            else -> {
                                Text("I don't know how to handle screen $screen!")
                            }
                        }
                    }
                }
            }
        })
    }
}