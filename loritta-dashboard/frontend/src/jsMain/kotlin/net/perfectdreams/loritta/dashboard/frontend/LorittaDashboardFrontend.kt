package net.perfectdreams.loritta.dashboard.frontend

import io.ktor.client.HttpClient
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.frontend.components.CharacterCounterComponent
import net.perfectdreams.loritta.dashboard.frontend.components.ColorPickerComponent
import net.perfectdreams.loritta.dashboard.frontend.components.CounterComponent
import net.perfectdreams.loritta.dashboard.frontend.components.DiscordMessageEditorComponent
import net.perfectdreams.loritta.dashboard.frontend.components.FancySelectMenuComponent
import net.perfectdreams.loritta.dashboard.frontend.components.LorittaShimejiComponent
import net.perfectdreams.loritta.dashboard.frontend.components.MischievousRascalsComponent
import net.perfectdreams.loritta.dashboard.frontend.components.RotatingImageComponent
import net.perfectdreams.loritta.dashboard.frontend.components.SaveBarComponent
import net.perfectdreams.loritta.dashboard.frontend.components.SidebarToggleComponent
import net.perfectdreams.loritta.dashboard.frontend.components.ToggleableSectionComponent
import net.perfectdreams.loritta.dashboard.frontend.components.TwitchCallbackListenerComponent
import net.perfectdreams.loritta.dashboard.frontend.compose.components.messages.DiscordMessageEditor
import net.perfectdreams.loritta.dashboard.frontend.modals.ModalManager
import net.perfectdreams.loritta.dashboard.frontend.soundeffects.SoundEffects
import net.perfectdreams.loritta.dashboard.frontend.toasts.ToastManager
import web.dom.document
import web.html.HTMLElement

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
        Bliss.registerComponent("mischievous-rascals") { MischievousRascalsComponent(this) }
        Bliss.registerComponent("twitch-callback-listener") { TwitchCallbackListenerComponent(this) }
        Bliss.processAttributes(document.body)

        toastManager.render(document.querySelector("#toast-list") as HTMLElement)
        modalManager.render(document.querySelector("#modal-list") as HTMLElement)
    }
}