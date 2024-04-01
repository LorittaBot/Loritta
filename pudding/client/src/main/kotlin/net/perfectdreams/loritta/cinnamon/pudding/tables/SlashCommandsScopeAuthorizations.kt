package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object SlashCommandsScopeAuthorizations : LongIdTable() {
    val guild = long("guild").index()
    val authorized = bool("authorized")
    val triggeredBy = long("triggered_by")
    val checkedAt = timestampWithTimeZone("checked_at")
}