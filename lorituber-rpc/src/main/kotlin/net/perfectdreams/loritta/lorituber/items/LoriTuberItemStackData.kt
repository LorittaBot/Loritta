package net.perfectdreams.loritta.lorituber.items

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import java.util.*

@Serializable
data class LoriTuberItemStackData(
    @Serializable(with = UUIDSerializer::class)
    val localId: UUID,
    var id: LoriTuberItemId,
    var quantity: Int,
    var behaviorAttributes: LoriTuberItemBehaviorAttributes? = null
)