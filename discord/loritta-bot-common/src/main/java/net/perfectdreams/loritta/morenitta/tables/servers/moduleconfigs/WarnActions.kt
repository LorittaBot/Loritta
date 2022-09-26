package net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs

import net.perfectdreams.loritta.morenitta.utils.exposed.rawJsonb
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object WarnActions : LongIdTable() {
    val config = reference("config", ModerationConfigs).index()
    val warnCount = integer("warn_count")
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val metadata = rawJsonb("metadata", gson).nullable()
}