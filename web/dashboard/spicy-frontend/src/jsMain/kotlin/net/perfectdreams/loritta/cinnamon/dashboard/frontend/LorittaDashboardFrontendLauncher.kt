package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.NoLiveLiterals
import js.core.jso
import kotlinx.browser.window
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Application

fun main() {
    val app = createGamePixiCanvas()

    val m = LorittaDashboardFrontend(app)
    m.start()
}

// This is in a different function because sometimes this fails to compile due to "live literals"
@NoLiveLiterals
private fun createGamePixiCanvas(): Application {
    val devicePixelRatio = window.devicePixelRatio

    // Create the Loritta shimeji-like overlay
    val width = window.innerWidth
    val height = window.innerHeight

    return Application(
        jso {
            this.width = width // width
            this.height = height // height
            this.backgroundAlpha = 0
            this.resolution = devicePixelRatio
            this.resizeTo = window
        }
    )
}