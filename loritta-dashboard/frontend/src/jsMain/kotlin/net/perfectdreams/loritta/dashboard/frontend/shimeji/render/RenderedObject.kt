package net.perfectdreams.loritta.dashboard.frontend.shimeji.render

import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import web.canvas.CanvasRenderingContext2D

abstract class RenderedObject(
    val m: GameState,
) {
    abstract val shouldBeRemoved: Boolean
    abstract val zIndex: Int

    abstract fun render(ctx: CanvasRenderingContext2D, isGameLogicUpdate: Boolean, deltaMS: Long, d: Double)
    abstract fun destroy()

    fun lerp(x: Double, y: Double, t: Double): Double {
        return x*(1-t)+y*t
    }
}