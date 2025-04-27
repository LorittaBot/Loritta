package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class GiveawayRoleExtraEntry(
    val roleId: Long,
    val weight: Int
)