package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards

import kotlinx.serialization.Serializable

@Serializable
data class RoleReward(
    val roleId: Long,
    val xp: Long
)