package net.perfectdreams.loritta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object ModerationConfigs : LongIdTable() {
    val sendPunishmentViaDm = bool("send_punishment_via_dm").default(false)
    val sendPunishmentToPunishLog = bool("send_punishment_to_punish_log").default(false)
    val punishLogChannelId = long("punish_log_channel").nullable()
    val punishLogMessage = text("punish_log_message").nullable()
}