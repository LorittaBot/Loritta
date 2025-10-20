package net.perfectdreams.loritta.dashboard.frontend.shimeji.entities

sealed class PlayerInput {
    class Jump(val power: Double) : PlayerInput()
    object Left : PlayerInput()
    object Right : PlayerInput()
}