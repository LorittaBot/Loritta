package net.perfectdreams.loritta.cinnamon.dashboard.common

import kotlinx.serialization.Serializable

@Serializable
data class ShipPercentage(val percentage: Int) {
    init {
        if (percentage !in 0..100)
            error("Ship percentage must be between 0..100")
    }
}