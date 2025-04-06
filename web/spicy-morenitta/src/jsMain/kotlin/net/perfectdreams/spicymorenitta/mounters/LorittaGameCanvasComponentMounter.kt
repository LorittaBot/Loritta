package net.perfectdreams.spicymorenitta.mounters

import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.utils.Logging
import web.events.Event
import web.events.addEventListener
import web.html.HTMLCanvasElement
import web.html.HTMLElement
import web.window.window

class LorittaGameCanvasComponentMounter(val m: SpicyMorenitta) : SimpleComponentMounter("loritta-game-canvas"), Logging {
    override fun simpleMount(element: HTMLElement) {
        // Technically only one loritta-game-canvas instance should exist
        if (element.getAttribute("loritta-powered-up") != null)
            return

        if (element !is HTMLCanvasElement)
            error("Game Canvas Component is not a HTMLCanvasElement!")

        element.setAttribute("loritta-powered-up", "")

        val pocketLorittaSettings = Json.decodeFromString<PocketLorittaSettings>(element.getAttribute("pocket-loritta-settings")!!)
        m.gameState.setCanvas(element.unsafeCast<org.w3c.dom.HTMLCanvasElement>())
        m.gameState.updateCanvasSize()
        m.gameState.syncStateWithSettings(pocketLorittaSettings)

        m.gameState.addedToTheDOM = true

        window.addEventListener(
            Event.RESIZE,
            {
                m.gameState.updateCanvasSize()
            }
        )

        m.gameState.start()
    }
}