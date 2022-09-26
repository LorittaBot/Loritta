package net.perfectdreams.loritta.legacy.tables.servers.moduleconfigs

import net.perfectdreams.loritta.legacy.tables.ServerConfigs
import net.perfectdreams.loritta.legacy.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object ModerationPunishmentMessagesConfig : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val punishLogMessage = text("punish_log_message")
}