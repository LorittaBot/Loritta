package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.*
import dev.kord.common.entity.optional.*
import kotlinx.serialization.Serializable

@Serializable
data class DeviousChannelData(
    val id: Snowflake,
    val type: ChannelType,
    val guildId: Snowflake?,
    val position: Int?,
    val permissionOverwrites: List<Overwrite>?,
    val name: String?,
    val topic: String?,
    val nsfw: Boolean
) {
    companion object {
        fun from(guildId: Snowflake?, data: DiscordChannel): DeviousChannelData {
            return DeviousChannelData(
                data.id,
                data.type,
                guildId,
                data.position.value,
                data.permissionOverwrites.value,
                data.name.value,
                data.topic.value,
                data.nsfw.discordBoolean
            )
        }
    }
}