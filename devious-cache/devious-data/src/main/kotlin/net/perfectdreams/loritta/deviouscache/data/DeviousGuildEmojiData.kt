package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class DeviousGuildEmojiData(
    val id: LightweightSnowflake,
    val name: String,
    val roles: List<LightweightSnowflake>?,
    val userId: LightweightSnowflake?,
    val managed: Boolean,
    val animated: Boolean,
    val available: Boolean
) {
    companion object {
        fun from(emoji: DiscordEmoji): DeviousGuildEmojiData {
            return DeviousGuildEmojiData(
                emoji.id!!.toLightweightSnowflake(),
                emoji.name!!,
                emoji.roles.value?.map { it.toLightweightSnowflake() },
                emoji.user.value?.id?.toLightweightSnowflake(), // This is null, even if you have the "Guild Members" intent (weird)
                emoji.managed.discordBoolean,
                emoji.animated.discordBoolean,
                emoji.available.discordBoolean
            )
        }
    }
}