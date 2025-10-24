package net.perfectdreams.loritta.dashboard.frontend

import io.ktor.client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.bliss.BlissBeforeBlissRequestPrepare
import net.perfectdreams.bliss.BlissProcessRequestJsonBody
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.frontend.components.CharacterCounterComponent
import net.perfectdreams.loritta.dashboard.frontend.components.CloseLeftSidebarOnClickComponent
import net.perfectdreams.loritta.dashboard.frontend.components.ColorPickerComponent
import net.perfectdreams.loritta.dashboard.frontend.components.CounterComponent
import net.perfectdreams.loritta.dashboard.frontend.components.DiscordMessageEditorComponent
import net.perfectdreams.loritta.dashboard.frontend.components.FancySelectMenuComponent
import net.perfectdreams.loritta.dashboard.frontend.components.LorittaShimejiActivityLevelComponent
import net.perfectdreams.loritta.dashboard.frontend.components.LorittaShimejiClearComponent
import net.perfectdreams.loritta.dashboard.frontend.components.LorittaShimejiComponent
import net.perfectdreams.loritta.dashboard.frontend.components.LorittaShimejiSpawnerComponent
import net.perfectdreams.loritta.dashboard.frontend.components.RotatingImageComponent
import net.perfectdreams.loritta.dashboard.frontend.components.SaveBarComponent
import net.perfectdreams.loritta.dashboard.frontend.components.SidebarToggleComponent
import net.perfectdreams.loritta.dashboard.frontend.components.ToggleableSectionComponent
import net.perfectdreams.loritta.dashboard.frontend.components.TwitchCallbackListenerComponent
import net.perfectdreams.loritta.dashboard.frontend.modals.ModalManager
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import net.perfectdreams.loritta.dashboard.frontend.soundeffects.SoundEffects
import net.perfectdreams.loritta.dashboard.frontend.toasts.Toast
import net.perfectdreams.loritta.dashboard.frontend.toasts.ToastManager
import org.jetbrains.compose.web.dom.Text
import web.animations.awaitAnimationFrame
import web.cssom.ClassName
import web.dom.document
import web.events.CustomEvent
import web.events.Event
import web.events.EventType
import web.events.addEventHandler
import web.history.PAGE_SHOW
import web.history.POP_STATE
import web.history.PageTransitionEvent
import web.history.PopStateEvent
import web.html.HTMLElement
import web.window.window

class LorittaDashboardFrontend {
    companion object {
        lateinit var INSTANCE: LorittaDashboardFrontend
    }

    val http = HttpClient {}
    val toastManager = ToastManager(this)
    val modalManager = ModalManager(this)
    val soundEffects = SoundEffects(this)

    fun start() {
        INSTANCE = this

        Bliss.registerComponent("counter") { CounterComponent() }
        Bliss.registerComponent("character-counter") { CharacterCounterComponent() }
        Bliss.registerComponent("toggleable-section") { ToggleableSectionComponent() }
        Bliss.registerComponent("save-bar") { SaveBarComponent() }
        Bliss.registerComponent("rotating-image") { RotatingImageComponent() }
        Bliss.registerComponent("fancy-select-menu") { FancySelectMenuComponent(this) }
        Bliss.registerComponent("color-picker") { ColorPickerComponent(this) }
        Bliss.registerComponent("discord-message-editor") { DiscordMessageEditorComponent(this) }
        Bliss.registerComponent("sidebar-toggle") { SidebarToggleComponent() }
        Bliss.registerComponent("loritta-shimeji") { LorittaShimejiComponent() }
        Bliss.registerComponent("twitch-callback-listener") { TwitchCallbackListenerComponent(this) }
        Bliss.registerComponent("close-left-sidebar-on-click") { CloseLeftSidebarOnClickComponent(this) }
        Bliss.registerComponent("loritta-shimeji-spawner") { LorittaShimejiSpawnerComponent(this) }
        Bliss.registerComponent("loritta-shimeji-clear") { LorittaShimejiClearComponent(this) }
        Bliss.registerComponent("loritta-shimeji-activity-level") { LorittaShimejiActivityLevelComponent(this) }
        Bliss.processAttributes(document.body)

        toastManager.render(document.querySelector("#toast-list") as HTMLElement)
        modalManager.render(document.querySelector("#modal-list") as HTMLElement)

        window.addEventHandler(PopStateEvent.POP_STATE) {
            // When pressing the back button, reload the entire page to avoid broken states
            window.location.reload()
        }

        document.addEventHandler(EventType<CustomEvent<BlissProcessRequestJsonBody>>("bliss:processRequestJsonBody")) {
            val detail = it.detail
            if (detail.element != null) {
                if (detail.element.getAttribute("loritta-include-spawner-settings") == "true") {
                    val gameState = (document.querySelector("[bliss-component='loritta-shimeji']").asDynamic().blissComponent as LorittaShimejiComponent).gameState

                    val lorittaPlayers = gameState.entities.filterIsInstance<LorittaPlayer>()
                    val lorittaCount = lorittaPlayers.count { it.playerType == LorittaPlayer.PlayerType.LORITTA }
                    val pantufaCount = lorittaPlayers.count { it.playerType == LorittaPlayer.PlayerType.PANTUFA }
                    val gabrielaCount = lorittaPlayers.count { it.playerType == LorittaPlayer.PlayerType.GABRIELA }

                    detail.map["lorittaCount"] = JsonPrimitive(lorittaCount)
                    detail.map["pantufaCount"] = JsonPrimitive(pantufaCount)
                    detail.map["gabrielaCount"] = JsonPrimitive(gabrielaCount)
                    detail.map["activityLevel"] = JsonPrimitive(gameState.activityLevel.name)

                    detail.includeBody = true
                }
            }
        }

        document.addEventHandler(EventType<CustomEvent<BlissBeforeBlissRequestPrepare>>("bliss:beforeBlissRequestPrepare")) {
            if (it.detail.element.getAttribute("loritta-cancel-if-save-bar-active") == "true" && SaveBarComponent.saveBarActive) {
                it.preventDefault()

                toastManager.showToast(
                    document.querySelector("#save-changes-warning-toast-template")!!
                        .getAttribute("bliss-toast")!!
                        .let {
                            Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(it))
                        }
                )

                val saveBar = document.querySelector("[bliss-component='save-bar']") as HTMLElement

                saveBar.classList.add(ClassName("attention"))
                GlobalScope.launch {
                    // Yes the delay is REQUIRED!!
                    awaitAnimationFrame()
                    saveBar.classList.remove(ClassName("attention"))
                }
                return@addEventHandler
            }
        }
    }
}