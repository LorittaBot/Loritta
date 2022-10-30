package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordVoiceState
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class DeviousVoiceStateData(val userId: Snowflake, val channelId: Snowflake) {
    companion object {
        fun from(data: DiscordVoiceState) = DeviousVoiceStateData(
            data.userId,
            data.channelId!!
        )
    }
}