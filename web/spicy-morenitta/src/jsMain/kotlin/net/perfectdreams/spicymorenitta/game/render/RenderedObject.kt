package net.perfectdreams.spicymorenitta.game.render

import net.perfectdreams.spicymorenitta.game.GameState
import org.w3c.dom.CanvasRenderingContext2D

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