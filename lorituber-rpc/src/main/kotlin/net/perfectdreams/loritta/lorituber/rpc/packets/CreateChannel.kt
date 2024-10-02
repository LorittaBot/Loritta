package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class CreateChannelRequest(
    val characterId: Long,
    val name: String
) : LoriTuberRequest()

@Serializable
sealed class CreateChannelResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        val id: Long,
        val name: String
    ) : CreateChannelResponse()

    @Serializable
    data object CharacterAlreadyHasTooManyChannels : CreateChannelResponse()
}