package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object ModerationLogs : LongIdTable() {
    val guildId = long("guild").index()
    val userId = long("user").index()
    val punisherId = long("punisher").index()
    val punishmentAction = enumerationByName<ModerationLogAction>("punishment_action", 64)
    val reason = text("reason").nullable()
    val timestamp = timestamp("timestamp")
    val expiresAt = timestampWithTimeZone("expires_at").nullable()
}
