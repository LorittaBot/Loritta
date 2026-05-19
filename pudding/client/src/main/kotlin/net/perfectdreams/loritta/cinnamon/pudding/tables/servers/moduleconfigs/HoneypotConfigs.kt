package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import net.perfectdreams.loritta.common.utils.PunishmentAction

object HoneypotConfigs : SnowflakeTable() {
    val enabled = bool("enabled").default(false).index()
    val action = enumeration("action", PunishmentAction::class).default(PunishmentAction.PURGE_KICK)
    val deleteDays = integer("delete_days").default(1)
    val reason = text("reason").nullable()
    val channels = array<Long>("channels").default(listOf())
}
