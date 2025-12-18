package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.luna.bliss.BlissComponent
import net.perfectdreams.luna.bliss.getBlissComponent
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLButtonElement
import web.pointer.CLICK
import web.pointer.PointerEvent

class LorittaShimejiClearComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLButtonElement>() {
    override fun onMount() {
        this.registeredEvents += this.mountedElement.addEventHandler(PointerEvent.CLICK) {
            val gameState = document.querySelector("[bliss-component='loritta-shimeji']")!!.getBlissComponent<LorittaShimejiComponent>().gameState

            while (gameState.entities.isNotEmpty()) {
                gameState.entities.removeFirst().remove()
            }
        }
    }

    override fun onUnmount() {}
}