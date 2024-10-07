package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberObjectData {
    @Serializable
    sealed class LoriTuberComputerData : LoriTuberObjectData() {
        // TODO: How to handle this?
        val canDoMultipleThingsAtTheSameTime: Boolean
            get() {
                TODO()
            }
    }
}