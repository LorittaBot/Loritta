package net.perfectdreams.loritta.legacy.tables.servers.moduleconfigs

import net.perfectdreams.loritta.legacy.utils.exposed.rawJsonb
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object WarnActions : LongIdTable() {
    val config = reference("config", ModerationConfigs).index()
    val warnCount = integer("warn_count")
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val metadata = rawJsonb("metadata", gson).nullable()
}