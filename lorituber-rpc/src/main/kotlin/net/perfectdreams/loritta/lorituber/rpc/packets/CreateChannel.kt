package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class CreateChannelRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    val name: String
) : LoriTuberRequest()

@Serializable
sealed class CreateChannelResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        @Serializable(UUIDSerializer::class)
        val id: UUID,
        val name: String
    ) : CreateChannelResponse()

    @Serializable
    data object CharacterAlreadyHasTooManyChannels : CreateChannelResponse()
}