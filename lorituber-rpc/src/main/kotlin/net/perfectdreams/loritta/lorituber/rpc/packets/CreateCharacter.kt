package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class CreateCharacterRequest(
    val userId: Long,
    val firstName: String,
    val lastName: String,
) : LoriTuberRequest()

@Serializable
sealed class CreateCharacterResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        @Serializable(UUIDSerializer::class)
        val id: UUID,
        val name: String
    ) : CreateCharacterResponse()

    @Serializable
    data object UserAlreadyHasTooManyCharacters : CreateCharacterResponse()
}