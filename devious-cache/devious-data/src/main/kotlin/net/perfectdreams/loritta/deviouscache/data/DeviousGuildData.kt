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
        fun from(data: DiscordGuild) = DeviousGuildData(
            data.id.toLightweightSnowflake(),
            data.name,
            data.ownerId.toLightweightSnowflake(),
            data.icon,
            data.vanityUrlCode,
            data.premiumSubscriptionCount.value ?: 0,
            // This is only available via the gateway GuildCreate event
            data.memberCount.value ?: 0,
            data.splash.value,
            data.banner,
        )
    }
}