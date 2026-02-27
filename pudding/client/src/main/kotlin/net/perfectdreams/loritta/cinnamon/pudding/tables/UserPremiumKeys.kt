package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserPremiumKeys : LongIdTable() {
	val userId = long("user").index()
	val value = integer("value")
	val expiresAt = timestampWithTimeZone("expires_at")
	val metadata = jsonb("metadata").nullable()
}