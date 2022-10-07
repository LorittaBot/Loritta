package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class DeviousMessageFragmentData(
    val id: Snowflake,
    val type: MessageType,
    val channelId: Snowflake,
    val guildId: Snowflake?,
    val content: String,
    val mentions: List<DiscordOptionallyMemberUser>,
    val reactions: List<Reaction>,
    val attachments: List<DiscordAttachment>,
    val pinned: Boolean,
    val referencedMessage: DeviousMessageFragmentData?
) {
    companion object {
        fun from(data: DiscordMessage): DeviousMessageFragmentData = DeviousMessageFragmentData(
            data.id,
            data.type,
            data.channelId,
            data.guildId.value,
            data.content,
            data.mentions,
            data.reactions.value ?: emptyList(),
            data.attachments,
            data.pinned,
            data.referencedMessage.value?.let { from(it) }
        )

        fun from(data: DiscordPartialMessage): DeviousMessageFragmentData = DeviousMessageFragmentData(
            data.id,
            data.type.value ?: MessageType.Unknown(-1),
            data.channelId,
            data.guildId.value,
            data.content.value ?: "", // Empty message yolo
            data.mentions.value ?: emptyList(),
            data.reactions.value ?: emptyList(),
            data.attachments.value ?: emptyList(),
            data.pinned.discordBoolean,
            data.referencedMessage.value?.let { from(it) }
        )
    }
}