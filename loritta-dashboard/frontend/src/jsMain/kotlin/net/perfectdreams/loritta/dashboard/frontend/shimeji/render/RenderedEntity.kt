package net.perfectdreams.loritta.dashboard.frontend.shimeji.render

import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.Entity

abstract class RenderedEntity<T : Entity>(
    m: GameState,
    val entity: T
) : RenderedObject(m) {
    var beginningX: Double = 0.0
    var beginningY: Double = 0.0
    var targetX: Double = 0.0
    var targetY: Double = 0.0
}