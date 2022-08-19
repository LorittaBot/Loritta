package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import org.jetbrains.exposed.dao.id.LongIdTable

object ModerationPredefinedPunishmentMessages : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val short = text("short").index()
    val message = text("message").index()
}