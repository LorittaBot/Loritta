package net.perfectdreams.loritta.serializable.lorituber.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberMail
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask

@Serializable
sealed interface LoriTuberRPCResponse {
    val currentTick: Long
    val lastUpdate: Long
}

// ===[ CHARACTERS ]===
@Serializable
data class GetCharactersByOwnerResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val characters: List<LoriTuberCharacter>
) : LoriTuberRPCResponse {
    @Serializable
    data class LoriTuberCharacter(
        val id: Long,
        val name: String
    )
}

@Serializable
data class GetCharacterStatusResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val name: String,
    val energy: Double,
    val hunger: Double,
    val currentTask: LoriTuberTask?
) : LoriTuberRPCResponse

@Serializable
sealed interface CreateCharacterResponse : LoriTuberRPCResponse {
    @Serializable
    data class Success(
        override val currentTick: Long,
        override val lastUpdate: Long,
        val id: Long,
        val name: String
    ) : CreateCharacterResponse

    @Serializable
    class UserAlreadyHasTooManyCharacters(
        override val currentTick: Long,
        override val lastUpdate: Long
    ) : CreateCharacterResponse
}

@Serializable
data class GetMailResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val mail: MailWrapper?
) : LoriTuberRPCResponse {
    @Serializable
    data class MailWrapper(
        val id: Long,
        val mail: LoriTuberMail
    )
}

@Serializable
data class AcknowledgeMailResponse(
    override val currentTick: Long,
    override val lastUpdate: Long
) : LoriTuberRPCResponse

@Serializable
sealed interface StartTaskResponse : LoriTuberRPCResponse {
    @Serializable
    data class Success(
        override val currentTick: Long,
        override val lastUpdate: Long
    ) : StartTaskResponse

    @Serializable
    class CharacterIsAlreadyDoingAnotherTask(
        override val currentTick: Long,
        override val lastUpdate: Long
    ) : StartTaskResponse
}

@Serializable
data class CancelTaskResponse(
    override val currentTick: Long,
    override val lastUpdate: Long
) : LoriTuberRPCResponse

@Serializable
sealed interface CreatePendingVideoResponse : LoriTuberRPCResponse {
    @Serializable
    data class Success(
        override val currentTick: Long,
        override val lastUpdate: Long,
        val videoId: Long
    ) : CreatePendingVideoResponse

    @Serializable
    class CharacterIsAlreadyDoingAnotherVideo(
        override val currentTick: Long,
        override val lastUpdate: Long
    ) : CreatePendingVideoResponse
}

// ===[ CHANNELS ]===
@Serializable
data class GetChannelsByCharacterResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val channels: List<LoriTuberChannel>
) : LoriTuberRPCResponse

@Serializable
data class GetChannelByIdResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val channel: LoriTuberChannel?
) : LoriTuberRPCResponse

@Serializable
sealed interface CreateChannelResponse : LoriTuberRPCResponse {
    @Serializable
    data class Success(
        override val currentTick: Long,
        override val lastUpdate: Long,
        val id: Long,
        val name: String
    ) : CreateChannelResponse

    @Serializable
    class CharacterAlreadyHasTooManyChannels(
        override val currentTick: Long,
        override val lastUpdate: Long
    ) : CreateChannelResponse
}

// ===[ MISC ]===
@Serializable
data class GetServerInfoResponse(
    override val currentTick: Long,
    override val lastUpdate: Long,
    val averageTickDuration: Double,
) : LoriTuberRPCResponse