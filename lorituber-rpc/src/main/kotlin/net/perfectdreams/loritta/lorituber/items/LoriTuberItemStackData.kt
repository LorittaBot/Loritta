package net.perfectdreams.loritta.lorituber.items

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberItemStackData(
    var id: LoriTuberItemId,
    var quantity: Int
)