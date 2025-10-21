package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.PunishmentAction

@Serializable
data class WarnAction(
    val count: Int,
    val action: PunishmentAction,
    val time: String?
)