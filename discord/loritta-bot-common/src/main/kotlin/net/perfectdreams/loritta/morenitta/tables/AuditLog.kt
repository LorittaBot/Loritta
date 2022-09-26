package net.perfectdreams.loritta.morenitta.tables

import net.perfectdreams.loritta.morenitta.utils.exposed.rawJsonb
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.utils.ActionType
import org.jetbrains.exposed.dao.id.LongIdTable

object AuditLog : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user")
	val executedAt = long("executed_at")
	val actionType = enumeration("action_type", ActionType::class)
	val params = rawJsonb("params", gson).nullable()
}