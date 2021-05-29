package net.perfectdreams.loritta.tables.servers.moduleconfigs

import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object ModerationPunishmentMessagesConfig : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val punishLogMessage = text("punish_log_message")
}