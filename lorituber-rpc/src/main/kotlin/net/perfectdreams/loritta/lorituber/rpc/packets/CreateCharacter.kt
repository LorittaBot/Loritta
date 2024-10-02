package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

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
        val id: Long,
        val name: String
    ) : CreateCharacterResponse()

    @Serializable
    data object UserAlreadyHasTooManyCharacters : CreateCharacterResponse()
}