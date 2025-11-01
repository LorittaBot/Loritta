package net.perfectdreams.loritta.dashboard.frontend

import js.array.asList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.bliss.BlissBeforeBlissRequestPrepare
import net.perfectdreams.bliss.BlissProcessRequestJsonBody
import net.perfectdreams.bliss.getBlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.frontend.components.*
import net.perfectdreams.loritta.dashboard.frontend.modals.ModalManager
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import net.perfectdreams.loritta.dashboard.frontend.soundeffects.SoundEffects
import net.perfectdreams.loritta.dashboard.frontend.toasts.ToastManager
import net.perfectdreams.loritta.dashboard.frontend.utils.isUserUsingAdblock
import web.animations.awaitAnimationFrame
import web.cssom.ClassName
import web.dom.document
import web.events.CustomEvent
import web.events.EventType
import web.events.addEventHandler
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent

class LorittaDashboardFrontend {
    companion object {
        lateinit var INSTANCE: LorittaDashboardFrontend
    }

    val toastManager = ToastManager(this)
    val modalManager = ModalManager(this)
    val soundEffects = SoundEffects(this)

    fun start() {
        INSTANCE = this

        if (isUserUsingAdblock())
            println("Stop using Adblock >:(")

        Bliss.setupEvents()

        Bliss.registerElementProcessor { element ->
            val modalOnClick = element.getAttribute("bliss-open-modal-on-click")

            if (modalOnClick != null) {
                val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-modal attribute on a bliss-open-modal-on-click!")
                val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))

                element.addEventHandler(PointerEvent.CLICK) {
                    this.modalManager.openModal(modal)
                }
            }
        }

        Bliss.registerElementProcessor { element ->
            val closeModalOnClick = element.getAttribute("bliss-close-modal-on-click")

            if (closeModalOnClick != null) {
                element.addEventHandler(PointerEvent.CLICK) {
                    this.modalManager.closeModal()
                }
            }
        }

        Bliss.registerElementProcessor { element ->
            val closeModalOnClick = element.getAttribute("bliss-close-all-modals-on-click")

            if (closeModalOnClick != null) {
                element.addEventHandler(PointerEvent.CLICK) {
                    this.modalManager.closeAllModals()
                }
            }
        }

        Bliss.registerElementProcessor { element ->
            val toastOnClick = element.getAttribute("bliss-show-toast-on-click")

            if (toastOnClick != null) {
                val content = element.getAttribute("bliss-toast") ?: error("Missing bliss-toast attribute on a bliss-show-toast-on-click!")
                val toast = Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(content))

                element.addEventHandler(PointerEvent.CLICK) {
                    this.toastManager.showToast(toast)
                }
            }
        }

        Bliss.registerDocumentParsedEventListener { parsedDocument ->
            val showToastHack = parsedDocument.querySelectorAll("[bliss-show-toast]")
            for (element in showToastHack.asList()) {
                val content = element.getAttribute("bliss-toast") ?: error("Missing bliss-toast attribute on a bliss-show-toast!")
                val toast = Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(content))
                element.remove()

                this.toastManager.showToast(toast)
            }

            val showModalHack = parsedDocument.querySelectorAll("[bliss-show-modal]")
            for (element in showModalHack.asList()) {
                val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-toast attribute on a bliss-modal!")
                val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))
                element.remove()

                this.modalManager.openModal(modal)
            }

            val closeModalHack = parsedDocument.querySelectorAll("[bliss-close-modal]")
            for (element in closeModalHack.asList()) {
                element.remove()
                this.modalManager.closeModal()
            }

            val closeAllModalsHack = parsedDocument.querySelectorAll("[bliss-close-all-modals]")
            for (element in closeAllModalsHack.asList()) {
                element.remove()
                this.modalManager.closeAllModals()
            }

            val playSoundEffectHack = parsedDocument.querySelectorAll("[bliss-sound-effect]").asList()
            for (element in playSoundEffectHack) {
                val sfx = element.getAttribute("bliss-sound-effect")!!
                element.remove()
                when (sfx) {
                    "configSaved" -> this.soundEffects.configSaved.play(1.0)
                    else -> error("Unknown SFX \"$sfx\"!")
                }
            }
        }

        document.addEventHandler(EventType<CustomEvent<String>>("loritta:showToast")) {
            toastManager.showToast(Json.decodeFromString(it.detail))
        }

        Bliss.registerComponent("counter") { CounterComponent() }
        Bliss.registerComponent("character-counter") { CharacterCounterComponent() }
        Bliss.registerComponent("toggleable-section") { ToggleableSectionComponent() }
        Bliss.registerComponent("save-bar") { SaveBarComponent() }
        Bliss.registerComponent("rotating-image") { RotatingImageComponent() }
        Bliss.registerComponent("fancy-select-menu") { FancySelectMenuComponent(this) }
        Bliss.registerComponent("color-picker") { ColorPickerComponent(this) }
        Bliss.registerComponent("discord-message-editor") { DiscordMessageEditorComponent(this) }
        Bliss.registerComponent("sidebar-toggle") { SidebarToggleComponent(this) }
        Bliss.registerComponent("loritta-shimeji") { LorittaShimejiComponent() }
        Bliss.registerComponent("twitch-callback-listener") { TwitchCallbackListenerComponent(this) }
        Bliss.registerComponent("close-left-sidebar-on-click") { CloseLeftSidebarOnClickComponent(this) }
        Bliss.registerComponent("loritta-shimeji-spawner") { LorittaShimejiSpawnerComponent(this) }
        Bliss.registerComponent("loritta-shimeji-clear") { LorittaShimejiClearComponent(this) }
        Bliss.registerComponent("loritta-shimeji-activity-level") { LorittaShimejiActivityLevelComponent(this) }
        Bliss.registerComponent("not-very-cash-money-blocker-replacement") { NotVeryCashMoneyBlockerReplacementComponent(this) }
        Bliss.processAttributes(document.body)

        toastManager.render(document.querySelector("#toast-list") as HTMLElement)
        modalManager.render(document.querySelector("#modal-list") as HTMLElement)

        document.addEventHandler(EventType<CustomEvent<BlissProcessRequestJsonBody>>("bliss:processRequestJsonBody")) {

            val detail = it.detail
            val element = detail.element
            if (element != null) {
                if (element.getAttribute("loritta-include-spawner-settings") == "true") {
                    val gameState = document.querySelector("[bliss-component='loritta-shimeji']")!!.getBlissComponent<LorittaShimejiComponent>().gameState

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

    fun isLeftSidebarOpen(): Boolean {
        val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")

        return sidebar.classList.contains(ClassName("is-open"))
    }

    fun openLeftSidebar() {
        val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")

        sidebar.classList.remove(ClassName("is-closed"))
        sidebar.classList.add(ClassName("is-open"))
        document.body.classList.add(ClassName("left-sidebar-open"))
    }

    fun closeLeftSidebar() {
        val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")

        sidebar.classList.remove(ClassName("is-open"))
        sidebar.classList.add(ClassName("is-closed"))
        document.body.classList.remove(ClassName("left-sidebar-open"))
    }
}