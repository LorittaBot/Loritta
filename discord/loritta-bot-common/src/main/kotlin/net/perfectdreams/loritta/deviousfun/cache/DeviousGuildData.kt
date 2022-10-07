package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.Snowflake
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
)