package net.perfectdreams.loritta.serializable.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberMail {
    @Serializable
    class BeginnerChannelCreated(
        val characterId: Long,
        val channelId: Long
    ) : LoriTuberMail()
}