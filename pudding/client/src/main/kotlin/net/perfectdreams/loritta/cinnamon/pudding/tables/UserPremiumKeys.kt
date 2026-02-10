package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserPremiumKeys : LongIdTable() {
	val userId = long("user").index()
	val credits = integer("credits")
	val expiresAt = timestampWithTimeZone("expires_at").index()
}