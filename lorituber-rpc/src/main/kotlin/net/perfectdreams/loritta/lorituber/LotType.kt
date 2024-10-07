package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed class LotType {
    @Serializable
    class Residential(
        @Serializable(UUIDSerializer::class)
        val ownerId: UUID
    ) : LotType()

    @Serializable
    data object Community : LotType()
}