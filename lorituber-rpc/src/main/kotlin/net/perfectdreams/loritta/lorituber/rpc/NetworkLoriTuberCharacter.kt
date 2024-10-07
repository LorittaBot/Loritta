package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class NetworkLoriTuberCharacter(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String
)