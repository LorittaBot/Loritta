package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import web.cssom.ClassName
import web.dom.document
import web.events.Event
import web.events.RESIZE
import web.events.addEventHandler
import web.events.addEventListener
import web.html.HTMLButtonElement
import web.html.HTMLCanvasElement
import web.html.HTMLDivElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.window.window

class LorittaShimejiComponent : BlissComponent<HTMLCanvasElement>() {
    val gameState = GameState()

    override fun onMount() {
        this.gameState.setCanvas(this.mountedElement)
        this.gameState.updateCanvasSize()
        this.gameState.spawnPlayer(LorittaPlayer.PlayerType.LORITTA)
        // this.gameState.syncStateWithSettings(pocketLorittaSettings)

        this.gameState.addedToTheDOM = true

        window.addEventListener(
            Event.RESIZE,
            {
                gameState.updateCanvasSize()
            }
        )

        gameState.start()
    }

    override fun onUnmount() {
        // TODO: Stop the game!
    }
}