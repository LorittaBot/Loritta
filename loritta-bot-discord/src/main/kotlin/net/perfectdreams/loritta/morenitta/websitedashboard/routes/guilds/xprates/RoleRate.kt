package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates

import kotlinx.serialization.Serializable

@Serializable
data class RoleRate(
    val roleId: Long,
    val rate: Double
)