package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.common.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object WarnActions : LongIdTable() {
    val config = reference("config", ModerationConfigs).index()
    val warnCount = integer("warn_count")
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val metadata = jsonb("metadata").nullable()
}