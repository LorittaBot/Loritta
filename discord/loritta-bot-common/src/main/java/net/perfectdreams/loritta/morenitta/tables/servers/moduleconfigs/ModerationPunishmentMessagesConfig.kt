package net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs

import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object ModerationPunishmentMessagesConfig : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val punishLogMessage = text("punish_log_message")
}