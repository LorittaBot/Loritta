package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData

@Serializable
data class LoriTuberPlaceData(
    val items: List<LoriTuberItemStackData>
)