package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LotType
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData

@Serializable
data class LoriTuberLotData(
    val type: LotType,
    val items: List<LoriTuberItemStackData>
)