package net.perfectdreams.loritta.dashboard.frontend.components

import kotlinx.serialization.json.Json
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
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
        val settings = this.mountedElement.getAttribute("loritta-shimeji-settings")!!.let {
            Json.decodeFromString<LorittaShimejiSettings>(BlissHex.decodeFromHexString(it))
        }

        this.gameState.setCanvas(this.mountedElement)
        this.gameState.updateCanvasSize()
        this.gameState.syncStateWithSettings(settings)

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