package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordVoiceState
import kotlinx.serialization.Serializable

@Serializable
data class DeviousVoiceStateData(val userId: LightweightSnowflake, val channelId: LightweightSnowflake) {
    companion object {
        fun from(data: DiscordVoiceState) = DeviousVoiceStateData(
            data.userId.toLightweightSnowflake(),
            data.channelId!!.toLightweightSnowflake()
        )
    }
}