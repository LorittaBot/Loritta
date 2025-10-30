package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.bliss.getBlissComponent
import net.perfectdreams.loritta.shimeji.ActivityLevel
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLButtonElement
import web.input.INPUT
import web.input.InputEvent

class LorittaShimejiActivityLevelComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLButtonElement>() {
    override fun onMount() {
        val gameState = document.querySelector("[bliss-component='loritta-shimeji']")!!.getBlissComponent<LorittaShimejiComponent>().gameState
        this.mountedElement.value = gameState.activityLevel.name

        this.registeredEvents += this.mountedElement.addEventHandler(InputEvent.INPUT) {
            val gameState = document.querySelector("[bliss-component='loritta-shimeji']")!!.getBlissComponent<LorittaShimejiComponent>().gameState

            gameState.activityLevel = ActivityLevel.valueOf(this.mountedElement.value)
        }
    }

    override fun onUnmount() {}
}