package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.exposed.rawJsonb
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.utils.ActionType
import org.jetbrains.exposed.dao.id.LongIdTable

object AuditLog : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user")
	val executedAt = long("executed_at")
	val actionType = enumeration("action_type", ActionType::class)
	val params = rawJsonb("params", gson).nullable()
}