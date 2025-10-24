package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLButtonElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import kotlin.random.Random

class LorittaShimejiSpawnerComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLButtonElement>() {
    override fun onMount() {
        val playerType = LorittaPlayer.PlayerType.valueOf(this.mountedElement.getAttribute("spawner-type")!!)

        this.registeredEvents += this.mountedElement.addEventHandler(PointerEvent.CLICK) {
            val gameState = (document.querySelector("[bliss-component='loritta-shimeji']").asDynamic().blissComponent as LorittaShimejiComponent).gameState

            gameState.spawnPlayer(playerType)

            m.soundEffects.spawnSqueak.play(
                1.0,
                playbackRate = Random.nextDouble(
                    0.9,
                    1.1
                )
            )
        }
    }

    override fun onUnmount() {}
}