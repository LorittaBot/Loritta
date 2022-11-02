package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import kotlinx.serialization.Serializable

@Serializable
data class DeviousChannelData(
    val id: LightweightSnowflake,
    val type: ChannelType,
    val guildId: LightweightSnowflake?,
    val position: Int?,
    val permissionOverwrites: List<DeviousOverwrite>?,
    val name: String?,
    val topic: String?,
    val nsfw: Boolean
) {
    companion object {
        fun from(guildId: Snowflake?, data: DiscordChannel): DeviousChannelData {
            return DeviousChannelData(
                data.id.toLightweightSnowflake(),
                data.type,
                guildId?.toLightweightSnowflake(),
                data.position.value,
                data.permissionOverwrites.value
                    ?.map {
                        DeviousOverwrite(
                            it.id.toLightweightSnowflake(),
                            it.type,
                            LightweightPermissions(it.allow),
                            LightweightPermissions(it.deny)
                        )
                    },
                data.name.value,
                data.topic.value,
                data.nsfw.discordBoolean
            )
        }
    }
}