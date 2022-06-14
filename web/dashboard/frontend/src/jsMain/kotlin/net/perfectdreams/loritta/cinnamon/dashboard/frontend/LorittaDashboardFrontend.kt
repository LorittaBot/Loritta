package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.MutableState
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.TestScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.RoutingManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.setJsonBody
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLDivElement

class LorittaDashboardFrontend {
    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)
    val http = HttpClient(Js) {
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
                        val activeModal = globalState.activeModal

                        if (activeModal != null) {
                            // Open modal if there is one present
                            Div(attrs = {
                                classes("modal-wrapper")

                                onClick {
                                    // Close modal when clicking outside of the screen
                                    globalState.activeModal = null
                                }
                            }) {
                                Div(attrs = {
                                    classes("modal")

                                    onClick {
                                        // Don't propagate the click to the modal wrapper
                                        it.stopPropagation()
                                    }
                                }) {
                                    Div(attrs = { classes("content") }) {
                                        activeModal.body.invoke()
                                    }

                                    Div(attrs = { classes("buttons-wrapper") }) {
                                        activeModal.buttons.forEach {
                                            it.invoke()
                                        }
                                    }
                                }
                            }
                        }

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

    suspend fun makeApiRequest(method: HttpMethod, path: String): LorittaResponse {
        val body = http.request("${window.location.origin}$path") {
            this.method = method
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend fun putLorittaRequest(path: String, request: LorittaRequest): LorittaResponse {
        val body = http.put("${window.location.origin}$path") {
            setJsonBody(request)
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend inline fun <reified T : LorittaResponse> makeApiRequestAndUpdateState(state: MutableState<State<T>>, method: HttpMethod, path: String) {
        state.value = State.Loading()
        val response = makeApiRequest(method, path)
        if (response is T)
            state.value = State.Success(response)
        else
            state.value = State.Failure(null)
    }
}