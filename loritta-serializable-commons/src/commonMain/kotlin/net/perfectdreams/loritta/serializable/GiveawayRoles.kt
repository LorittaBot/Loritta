package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class GiveawayRoles(
    val roleIds: List<Long>,
    val isAndCondition: Boolean
)