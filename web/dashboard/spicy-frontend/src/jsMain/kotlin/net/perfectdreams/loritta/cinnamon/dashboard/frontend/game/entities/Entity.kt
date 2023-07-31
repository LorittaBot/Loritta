package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameState

abstract class Entity(
    val m: GameState,
    var x: Int,
    var y: Int
) {
    var dead = false
    abstract val width: Int
    abstract val height: Int

    abstract fun tick()

    abstract fun remove()
}