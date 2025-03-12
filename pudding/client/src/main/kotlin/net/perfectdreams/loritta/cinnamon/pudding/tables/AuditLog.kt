package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.common.utils.ActionType
import org.jetbrains.exposed.dao.id.LongIdTable

object AuditLog : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user")
	val executedAt = long("executed_at")
	val actionType = enumeration("action_type", ActionType::class)
	val params = jsonb("params").nullable()
}