package net.perfectdreams.loritta.lorituber.server.state.entities

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import java.util.*

data class LoriTuberVideo(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val data: LoriTuberVideoData
) : LoriTuberEntity()