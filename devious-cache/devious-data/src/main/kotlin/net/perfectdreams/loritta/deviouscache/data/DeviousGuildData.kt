package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordGuild
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import kotlinx.serialization.Serializable

@Serializable
data class DeviousGuildData(
    val id: Snowflake,
    val name: String,
    val ownerId: Snowflake,
    val icon: String?,
    val vanityUrlCode: String?,
    val premiumSubscriptionCount: Int,
    val memberCount: Int,
    val splashId: String?,
    val bannerId: String?
) {
    companion object {
        fun from(data: DiscordGuild) = DeviousGuildData(
            data.id,
            data.name,
            data.ownerId,
            data.icon,
            data.vanityUrlCode,
            data.premiumSubscriptionCount.value ?: 0,
            data.memberCount.value ?: data.approximateMemberCount.value ?: 0,
            data.splash.value,
            data.banner,
        )
    }
}