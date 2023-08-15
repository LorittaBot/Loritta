package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.GuildLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserRightSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify.GamerSaferVerify
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.guilds.ChooseAServerOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop.SonhosShopOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Application
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.*

class LorittaDashboardFrontend(private val app: Application) {
    companion object {
        private val logger = KotlinLogging.loggerClassName(LorittaDashboardFrontend::class)
    }

    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)

    val http = HttpClient(Js) {
        expectSuccess = false
    }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }
    val gameState = GameState(app)

    val configSavedSfx: Audio by lazy { Audio("${window.location.origin}/assets/snd/config-saved.ogg") }
    val configErrorSfx: Audio by lazy { Audio("${window.location.origin}/assets/snd/config-error.ogg") }

    private fun appendGameOverlay() {
        app.view.addClass("loritta-game-canvas")
        document.body!!.appendChild(app.view)
    }

    fun start() {
        logger.info { "Howdy from Kotlin ${KotlinVersion.CURRENT}! :3" }

        NitroPayUtils.prepareNitroPayState()

        appendGameOverlay()

        globalState.launch {
            globalState.launch { globalState.updateSelfUserInfo() }
            globalState.launch { globalState.updateSpicyInfo() }

            // We need to get it in this way, because we want to get the i18nContext for the routing manager
            val i18nContext = globalState.retrieveI18nContext()
            globalState.i18nContext = Resource.Success(i18nContext)

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

            if (userInfo !is Resource.Success || i18nContext !is Resource.Success || spicyInfo !is Resource.Success) {
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
                                                if (it.target == it.currentTarget)
                                                    globalState.activeModal = null
                                            }
                                        }) {
                                            Div(attrs = {
                                                classes("modal")
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

                                val screen = routingManager.screenState

                                when (screen) {
                                    is UserScreen -> {
                                        UserLeftSidebar(this@LorittaDashboardFrontend)

                                        UserRightSidebar(this@LorittaDashboardFrontend) {
                                            when (screen) {
                                                is ChooseAServerScreen -> {
                                                    ChooseAServerOverview(
                                                        this@LorittaDashboardFrontend,
                                                        screen,
                                                        i18nContext.value
                                                    )
                                                }
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
                                            }
                                        }
                                    }
                                    is GuildScreen -> {
                                        // Always recompose if it is a new guild ID, to force the guild data to be reloaded
                                        key(screen.guildId) {
                                            val vm = viewModel { GuildViewModel(this@LorittaDashboardFrontend, it, screen.guildId) }
                                            val guildInfoResource = vm.guildInfoResource

                                            val guild = ((guildInfoResource as? Resource.Success)?.value as? LorittaDashboardRPCResponse.GetGuildInfoResponse.Success)?.guild

                                            GuildLeftSidebar(this@LorittaDashboardFrontend, screen, guild)

                                            UserRightSidebar(this@LorittaDashboardFrontend) {
                                                when (screen) {
                                                    is ConfigureGuildGamerSaferVerifyScreen -> {
                                                        GamerSaferVerify(
                                                            this@LorittaDashboardFrontend,
                                                            screen,
                                                            i18nContext.value,
                                                            vm
                                                        )
                                                    }
                                                }
                                            }
                                        }
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

    suspend inline fun <reified T : LorittaDashboardRPCResponse> makeRPCRequest(request: LorittaDashboardRPCRequest): T {
        val body = http.post("${window.location.origin}/api/v1/rpc") {
            setBody(
                Json.encodeToString<LorittaDashboardRPCRequest>(
                    request
                )
            )
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend inline fun <reified T : LorittaDashboardRPCResponse> makeRPCRequestAndUpdateState(resource: MutableState<Resource<T>>, request: LorittaDashboardRPCRequest) = makeRPCRequestAndUpdateStateCheckType<T, T>(
        resource,
        request
    )

    suspend inline fun <reified D : LorittaDashboardRPCResponse, reified T : LorittaDashboardRPCResponse> makeRPCRequestAndUpdateStateCheckType(resource: MutableState<Resource<T>>, request: LorittaDashboardRPCRequest) {
        resource.value = Resource.Loading()
        val response = try {
            makeRPCRequest<D>(request)
        } catch (e: Exception) {
            resource.value = Resource.Failure(e)
            return
        }

        if (response !is T) {
            resource.value = Resource.Failure(RuntimeException("Deserialized $response does not match the type!"))
        } else {
            resource.value = Resource.Success(response)
        }
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

    suspend inline fun <reified T : LorittaResponse> makeApiRequestAndUpdateState(resource: MutableState<Resource<T>>, method: HttpMethod, path: String) {
        resource.value = Resource.Loading()
        val response = makeApiRequest(method, path)
        if (response is T)
            resource.value = Resource.Success(response)
        else
            resource.value = Resource.Failure(null)
    }
}