package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
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