package net.perfectdreams.loritta.dashboard.frontend.components

import kotlinx.serialization.json.Json
import net.perfectdreams.luna.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import web.events.Event
import web.events.RESIZE
import web.events.addEventListener
import web.html.HTMLCanvasElement
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