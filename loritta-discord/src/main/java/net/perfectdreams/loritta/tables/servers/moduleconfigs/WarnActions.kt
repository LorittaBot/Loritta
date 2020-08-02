package net.perfectdreams.loritta.tables.servers.moduleconfigs

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.dao.id.LongIdTable

object WarnActions : LongIdTable() {
    val config = reference("config", ModerationConfigs).index()
    val warnCount = integer("warn_count")
    val punishmentAction = enumeration("punishment_action", PunishmentAction::class)
    val metadata = rawJsonb("metadata", gson).nullable()
}