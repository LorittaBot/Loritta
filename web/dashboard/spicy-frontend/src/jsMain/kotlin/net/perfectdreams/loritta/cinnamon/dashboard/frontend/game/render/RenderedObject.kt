package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render

import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Container
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.PixiSprite
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.PixiTexture

abstract class RenderedObject(
    val spriteContainer: Container,
    val nametagContainer: Container
) {
    abstract val shouldBeRemoved: Boolean

    abstract fun render(isGameLogicUpdate: Boolean, deltaMS: Long, d: Double)
    abstract fun changeTexture(newTexture: PixiTexture)
    abstract fun destroy()

    fun lerp(x: Double, y: Double, t: Double): Double {
        return x*(1-t)+y*t
    }
}