package net.perfectdreams.loritta.dashboard.frontend.components

import kotlinx.serialization.json.Json
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.utils.isUserUsingAdBlock
import org.jetbrains.compose.web.dom.clear
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLImageElement
import web.pointer.CLICK
import web.pointer.PointerEvent

class NotVeryCashMoneyBlockerReplacementComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLDivElement>() {
    override fun onMount() {
        val imagesToBeReplaced = this.mountedElement.getAttribute("not-very-cash-money-blocker-replacement-images")!!.split(",").map { it.trim() }

        if (isUserUsingAdBlock()) {
            this.mountedElement.clear()
            this.mountedElement.style.display = "none"

            val imgWrapperElement = document.createElement("div").apply {
                this as HTMLDivElement
                this.style.display = "flex"
                this.style.justifyContent = "center"
                this.style.alignItems = "center"
            }

            val imgElement = document.createElement("img").apply {
                this as HTMLImageElement
                this.style.cursor = "pointer"
                this.src = imagesToBeReplaced.random()
            }

            val content = this.mountedElement.getAttribute("bliss-modal") ?: error("Missing bliss-modal attribute on a bliss-open-modal-on-click!")
            val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))

            this.registeredEvents += imgElement.addEventHandler(PointerEvent.CLICK) {
                it.preventDefault()

                m.modalManager.openModal(modal)
            }

            imgWrapperElement.appendChild(imgElement)
            this.mountedElement.parentElement!!
                .after(imgWrapperElement)
        }
    }

    override fun onUnmount() {}
}