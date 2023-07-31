package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities

sealed class PlayerInput {
    class Jump(val power: Double) : PlayerInput()
    object Left : PlayerInput()
    object Right : PlayerInput()
}