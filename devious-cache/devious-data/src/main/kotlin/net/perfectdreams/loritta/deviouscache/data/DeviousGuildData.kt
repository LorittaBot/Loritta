package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordGuild
import dev.kord.common.entity.optional.value
import kotlinx.serialization.Serializable

@Serializable
data class DeviousGuildData(
    val id: LightweightSnowflake,
    val name: String,
    val ownerId: LightweightSnowflake,
    val icon: String?,
    val vanityUrlCode: String?,
    val premiumSubscriptionCount: Int,
    val memberCount: Int,
    val splashId: String?,
    val bannerId: String?
) {
    companion object {
        // Member Count is only available via the GuildCreate gateway event
        fun from(
            data: DiscordGuild,
            premiumSubscriptionCount: Int,
            memberCount: Int
        ) = DeviousGuildData(
            data.id.toLightweightSnowflake(),
            data.name,
            data.ownerId.toLightweightSnowflake(),
            data.icon,
            data.vanityUrlCode,
            premiumSubscriptionCount,
            memberCount,
            data.splash.value,
            data.banner,
        )
    }
}