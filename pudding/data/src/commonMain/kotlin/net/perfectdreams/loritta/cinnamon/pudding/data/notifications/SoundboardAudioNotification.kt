package net.perfectdreams.loritta.cinnamon.pudding.data.notifications

import kotlinx.serialization.Serializable

@Serializable
class SoundboardAudioRequest(
    override val uniqueId: String,
    val guildId: Long,
    val channelId: Long,
    val audio: SoundboardAudio
) : LorittaNotification()

enum class SoundboardAudio {
    AMONG_US_ROUND_START,
    RAPAIZ,
    CHAVES_RISADAS,
    DANCE_CAT_DANCE,
    ESSE_E_O_MEU_PATRAO_HEHE,
    IRRA,
    RATINHO,
    UEPA,
    UI,
    NICELY_DONE_CHEER
}