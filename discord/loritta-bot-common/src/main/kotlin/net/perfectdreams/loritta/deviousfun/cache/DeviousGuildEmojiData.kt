package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class DeviousGuildEmojiData(
    val id: Snowflake,
    val name: String,
    val roles: List<Snowflake>?,
    val userId: Snowflake?,
    val managed: Boolean,
    val animated: Boolean,
    val available: Boolean
) {
    companion object {
        fun from(emoji: DiscordEmoji): DeviousGuildEmojiData {
            return DeviousGuildEmojiData(
                emoji.id!!,
                emoji.name!!,
                emoji.roles.value,
                emoji.user.value?.id, // This is null, even if you have the "Guild Members" intent (weird)
                emoji.managed.discordBoolean,
                emoji.animated.discordBoolean,
                emoji.available.discordBoolean
            )
        }
    }
}