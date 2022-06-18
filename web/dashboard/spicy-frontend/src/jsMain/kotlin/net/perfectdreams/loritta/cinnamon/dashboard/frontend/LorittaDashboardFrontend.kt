package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.CompositionLocalProvider
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
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop.SonhosShopOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.SonhosShopScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalSpicyInfo
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.NitroPayUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.RoutingManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.loggerClassName
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.setJsonBody
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.COMPLETE
import org.w3c.dom.DocumentReadyState
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.INTERACTIVE

class LorittaDashboardFrontend {
    companion object {
        private val logger = KotlinLogging.loggerClassName(LorittaDashboardFrontend::class)
    }

    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)
    val http = HttpClient(Js) {
        expectSuccess = false
    }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }

    fun start() {
        logger.info { "Howdy from Kotlin ${KotlinVersion.CURRENT}! :3" }

        NitroPayUtils.prepareNitroPayState()

        globalState.launch {
            globalState.launch { globalState.updateSelfUserInfo() }
            globalState.launch { globalState.updateSpicyInfo() }

            // We need to get it in this way, because we want to get the i18nContext for the routing manager
            val i18nContext = globalState.retrieveI18nContext()
            globalState.i18nContext = State.Success(i18nContext)

            // Switch based on the path
            routingManager.switchBasedOnPath(i18nContext, "/${window.location.pathname.split("/").drop(2).joinToString("/")}", false)

            window.onpopstate = {
                // TODO: We need to get the current i18nContext state from the globalState
                routingManager.switchBasedOnPath(i18nContext, "/${(it.state as String).split("/").drop(2).joinToString("/")}", true)
            }

            runOnDOMLoaded {
                logger.info { "DOM has been loaded! Mounting Jetpack Compose Web..." }

                renderApp()
            }
        }
    }

    fun renderApp() {
        renderComposable(rootElementId = "root") {
            val userInfo = globalState.userInfo
            val spicyInfo = globalState.spicyInfo
            val i18nContext = globalState.i18nContext

            if (userInfo !is State.Success || i18nContext !is State.Success || spicyInfo !is State.Success) {
                Text("Loading...")
            } else {
                CompositionLocalProvider(LocalI18nContext provides i18nContext.value) {
                    CompositionLocalProvider(LocalUserIdentification provides userInfo.value) {
                        CompositionLocalProvider(LocalSpicyInfo provides spicyInfo.value) {
                            // Fade out the single page application loading wrapper...
                            spaLoadingWrapper?.addClass("loaded")

                            Div(attrs = { id("wrapper") }) {
                                // Wrapped in a div to only trigger a recomposition within this div when a modal is updated
                                Div {
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
                                                    Div(attrs = { classes("title") }) {
                                                        Text(activeModal.title)
                                                    }

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
                                }

                                when (val screen = routingManager.screenState) {
                                    is ShipEffectsScreen -> {
                                        ShipEffectsOverview(
                                            this@LorittaDashboardFrontend,
                                            screen,
                                            i18nContext.value
                                        )
                                    }
                                    is SonhosShopScreen -> {
                                        SonhosShopOverview(
                                            this@LorittaDashboardFrontend,
                                            screen,
                                            i18nContext.value
                                        )
                                    }
                                    else -> {
                                        Text("I don't know how to handle screen $screen")
                                        if (screen != null)
                                            Text(" (${screen::class})")
                                        Text("!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // https://stackoverflow.com/a/59220393/7271796
    private fun runOnDOMLoaded(block: () -> (Unit)) {
        logger.info { "Current document readyState is ${document.readyState}" }
        if (document.readyState == DocumentReadyState.INTERACTIVE || document.readyState == DocumentReadyState.COMPLETE) {
            // already fired, so run logic right away
            block.invoke()
        } else {
            // not fired yet, so let's listen for the event
            window.addEventListener("DOMContentLoaded", { block.invoke() })
        }
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

    suspend fun postLorittaRequest(path: String, request: LorittaRequest): LorittaResponse {
        val body = http.post("${window.location.origin}$path") {
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