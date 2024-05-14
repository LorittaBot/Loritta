package net.perfectdreams.spicymorenitta.game.render

import net.perfectdreams.spicymorenitta.game.GameState
import net.perfectdreams.spicymorenitta.game.entities.Entity

abstract class RenderedEntity<T : Entity>(
    m: GameState,
    val entity: T
) : RenderedObject(m) {
    var beginningX: Double = 0.0
    var beginningY: Double = 0.0
    var targetX: Double = 0.0
    var targetY: Double = 0.0
}