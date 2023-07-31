package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.Entity
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Container
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.PixiSprite

abstract class RenderedEntity<T : Entity>(
    spriteContainer: Container,
    nametagContainer: Container,
    val entity: T
) : RenderedObject(spriteContainer, nametagContainer) {
    abstract val sprite: PixiSprite
    var beginningX: Double = 0.0
    var beginningY: Double = 0.0
    var targetX: Double = 0.0
    var targetY: Double = 0.0
}