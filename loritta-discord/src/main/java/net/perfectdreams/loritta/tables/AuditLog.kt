package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import net.perfectdreams.loritta.utils.ActionType
import org.jetbrains.exposed.dao.id.LongIdTable

object AuditLog : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user")
	val executedAt = long("executed_at")
	val actionType = enumeration("action_type", ActionType::class)
	val params = rawJsonb("params", gson).nullable()
}