package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.GuildLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserRightSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands.AddNewGuildCustomCommand
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands.EditGuildCustomCommand
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands.GuildCustomCommands
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify.GamerSaferVerify
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.guilds.ChooseAServerOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop.SonhosShopOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.starboard.GuildStarboard
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.welcomer.GuildWelcomer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Application
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.COMPLETE
import org.w3c.dom.DocumentReadyState
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.INTERACTIVE
import kotlin.random.Random

class LorittaDashboardFrontend(private val app: Application) {
    companion object {
        private val logger = KotlinLogging.loggerClassName(LorittaDashboardFrontend::class)
        lateinit var INSTANCE: LorittaDashboardFrontend

        /**
         * If enabled, the canTalk permission check in the channel select menu will always fail.
         *
         * Useful to test the "Missing Permission" modal!
         */
        var shouldChannelSelectMenuPermissionCheckAlwaysFail = false
    }

    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)

    val http = HttpClient(Js) {
        expectSuccess = false
    }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }
    val gameState = GameState(app)

    val soundEffects = SoundEffects(this)

    private fun appendGameOverlay() {
        app.view.addClass("loritta-game-canvas")
        document.body!!.appendChild(app.view)
    }

    fun start() {
        INSTANCE = this
        logger.info { "Howdy from Kotlin ${KotlinVersion.CURRENT}! :3" }

        NitroPayUtils.prepareNitroPayState()

        appendGameOverlay()

        val selectedTheme = localStorage.getItem("dashboard.selectedTheme")?.let {
            ColorTheme.valueOf(it)
        }

        if (selectedTheme != null)
            globalState.theme = selectedTheme
        else
            globalState.openThemeSelectorModal(true)

        globalState.launch {
            globalState.launch { globalState.updateSelfUserInfo() }
            globalState.launch { globalState.updateSpicyInfo() }

            // We need to get it in this way, because we want to get the i18nContext for the routing manager
            val i18nContext = globalState.retrieveI18nContext()
            globalState.i18nContext = Resource.Success(i18nContext)

            // Switch based on the path
            routingManager.switchBasedOnPath(i18nContext, "/${window.location.pathname.split("/").drop(2).joinToString("/")}${window.location.search}", false)

            window.onpopstate = {
                // TODO: We need to get the current i18nContext state from the globalState
                routingManager.switchBasedOnPath(i18nContext, "/${(it.state as String).split("/").drop(2).joinToString("/")}", true)
            }

            runOnDOMLoaded {
                logger.info { "DOM has been loaded! Mounting Jetpack Compose HTML..." }

                renderApp()
            }
        }
    }

    fun renderApp() {
        renderComposable(rootElementId = "root") {
            // Add the "modal-open" class to the body when a modal is active
            // Only run the side effect if the active modal has changed
            SideEffect {
                // Yes, this NEEDS to be added to the body, we can't just apply it to the app-wrapper (trust me, I tried)
                if (globalState.activeModals.isNotEmpty()) {
                    document.body!!.addClass("modal-open")
                } else {
                    document.body!!.removeClass("modal-open")
                }
            }

            Div(attrs = {
                id("app-wrapper")
                classes(
                    when (globalState.theme) {
                        ColorTheme.LIGHT -> "light-theme"
                        ColorTheme.DARK -> "dark-theme"
                    }
                )
            }) {
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
                                        for (activeModal in globalState.activeModals) {
                                            // Open modal if there is one present
                                            Div(attrs = {
                                                classes("modal-wrapper")

                                                if (activeModal.canBeClosedByClickingOutsideTheWindow) {
                                                    onClick {
                                                        // Close modal when clicking outside of the screen
                                                        if (it.target == it.currentTarget)
                                                            activeModal.close()
                                                    }
                                                }
                                            }) {
                                                Div(attrs = {
                                                    classes("modal")
                                                }) {
                                                    Div(attrs = { classes("content") }) {
                                                        Div(attrs = { classes("title") }) {
                                                            Text(activeModal.title)
                                                        }

                                                        activeModal.body.invoke(activeModal)
                                                    }

                                                    if (activeModal.buttons.isNotEmpty()) {
                                                        Div(attrs = { classes("buttons-wrapper") }) {
                                                            activeModal.buttons.forEach {
                                                                it.invoke(activeModal)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Div(attrs = {
                                        classes("toast-list")

                                        if (globalState.activeSaveBar)
                                            classes("save-bar-active")
                                    }) {
                                        for (toastWithAnimationState in globalState.activeToasts) {
                                            // We need to key it based on the ID to avoid Compose recomposing the toast notification during an animation
                                            // https://kotlinlang.slack.com/archives/C01F2HV7868/p1694583087487209
                                            key(toastWithAnimationState.toastId) {
                                                LaunchedEffect(Unit) {
                                                    soundEffects.toastNotificationWhoosh.play(
                                                        0.05,
                                                        Random.nextDouble(0.975, 1.025) // Change the speed/pitch to avoid the sound effect sounding repetitive
                                                    )
                                                }

                                                Div(attrs = {
                                                    id("toast-${toastWithAnimationState.toastId}")
                                                    classes(
                                                        "toast",
                                                        when (toastWithAnimationState.toast.type) {
                                                            Toast.Type.INFO -> "info"
                                                            Toast.Type.SUCCESS -> "success"
                                                            Toast.Type.WARN -> "warn"
                                                        }
                                                    )

                                                    when (toastWithAnimationState.state.value) {
                                                        GlobalState.ToastWithAnimationState.State.ADDED -> {
                                                            classes("added")
                                                            onAnimationEnd {
                                                                println("Finished toast (added) animation!")
                                                                toastWithAnimationState.state.value =
                                                                    GlobalState.ToastWithAnimationState.State.DEFAULT
                                                            }
                                                        }

                                                        GlobalState.ToastWithAnimationState.State.DEFAULT -> {
                                                            // I'm just happy to be here
                                                        }

                                                        GlobalState.ToastWithAnimationState.State.REMOVED -> {
                                                            classes("removed")
                                                            onAnimationEnd {
                                                                println("Finished toast (removed) animation!")
                                                                globalState.activeToasts.remove(toastWithAnimationState)
                                                            }
                                                        }
                                                    }
                                                }) {
                                                    Div(attrs = {
                                                        classes("toast-title")
                                                    }) {
                                                        Text(toastWithAnimationState.toast.title)
                                                    }

                                                    Div {
                                                        toastWithAnimationState.toast.body.invoke()
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
                                                val vm = viewModel {
                                                    GuildViewModel(
                                                        this@LorittaDashboardFrontend,
                                                        it,
                                                        screen.guildId
                                                    )
                                                }
                                                val guildInfoResource = vm.guildInfoResource

                                                val guild = (guildInfoResource as? Resource.Success)?.value

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

                                                        is ConfigureGuildWelcomerScreen -> {
                                                            GuildWelcomer(
                                                                this@LorittaDashboardFrontend,
                                                                screen,
                                                                i18nContext.value,
                                                                vm
                                                            )
                                                        }

                                                        is ConfigureGuildStarboardScreen -> {
                                                            GuildStarboard(
                                                                this@LorittaDashboardFrontend,
                                                                screen,
                                                                i18nContext.value,
                                                                vm
                                                            )
                                                        }

                                                        is ConfigureGuildCustomCommandsScreen -> {
                                                            GuildCustomCommands(
                                                                this@LorittaDashboardFrontend,
                                                                screen,
                                                                i18nContext.value,
                                                                vm
                                                            )
                                                        }

                                                        is AddNewGuildCustomCommandScreen -> {
                                                            AddNewGuildCustomCommand(
                                                                this@LorittaDashboardFrontend,
                                                                screen,
                                                                i18nContext.value,
                                                                vm
                                                            )
                                                        }

                                                        is EditGuildCustomCommandScreen -> {
                                                            EditGuildCustomCommand(
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
        val response = Json.decodeFromString<LorittaDashboardRPCResponse>(body)
        if (response !is T)
            error("Response is not ${T::class}!")
        return response
    }

    suspend inline fun <reified T : DashGuildScopedResponse> makeGuildScopedRPCRequest(guildId: Long, request: DashGuildScopedRequest): T {
        val body = http.post("${window.location.origin}/api/v1/rpc") {
            setBody(
                Json.encodeToString<LorittaDashboardRPCRequest>(
                    LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                        guildId,
                        request
                    )
                )
            )
        }.bodyAsText()
        val response = Json.decodeFromString<LorittaDashboardRPCResponse>(body)
        if (response !is LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse)
            error("Response is not a ExecuteDashGuildScopedRPCResponse!")
        val dashResponse = response.dashResponse
        if (dashResponse !is T)
            error("Response is not ${T::class}!")
        return dashResponse
    }

    suspend inline fun <reified T : DashGuildScopedResponse> makeGuildScopedRPCRequestWithGenericHandling(
        guildId: Long,
        request: DashGuildScopedRequest,
        onSuccess: (T) -> (Unit),
        onError: (DashGuildScopedResponse) -> (Unit)
    ) {
        val body = http.post("${window.location.origin}/api/v1/rpc") {
            setBody(
                Json.encodeToString<LorittaDashboardRPCRequest>(
                    LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                        guildId,
                        request
                    )
                )
            )
        }.bodyAsText()
        val response = Json.decodeFromString<LorittaDashboardRPCResponse>(body)
        if (response !is LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse)
            error("Response is not a ExecuteDashGuildScopedRPCResponse!")
        when (val dashResponse = response.dashResponse) {
            DashGuildScopedResponse.InvalidDiscordAuthorization -> {
                globalState.showToast(Toast.Type.WARN, "Autorização inválida!") {
                    Text("Eu acho que a autorização expirou, tente recarregar a página!")
                }
                onError.invoke(dashResponse)
            }
            DashGuildScopedResponse.MissingPermission -> {
                globalState.showToast(Toast.Type.WARN, "Sem permissão!") {
                    Text("Você não tem permissão para realizar isto!")
                }
                onError.invoke(dashResponse)
            }
            DashGuildScopedResponse.UnknownGuild -> {
                globalState.showToast(Toast.Type.WARN, "Servidor desconhecido!") {
                    Text("O servidor não existe!")
                }
                onError.invoke(dashResponse)
            }
            DashGuildScopedResponse.UnknownMember -> {
                globalState.showToast(Toast.Type.WARN, "Membro desconhecido!") {
                    Text("O membro não está no servidor!")
                }
                onError.invoke(dashResponse)
            }
            else -> {
                if (dashResponse !is T)
                    error("Response is not ${T::class}!")
                onSuccess.invoke(dashResponse)
            }
        }
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