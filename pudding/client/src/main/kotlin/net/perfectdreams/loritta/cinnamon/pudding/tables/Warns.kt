package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object Warns : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val punishedById = long("punished_by")
	val content = text("content").nullable()
	val receivedAt = long("received_at")
	val expiresAt = timestampWithTimeZone("expires_at").nullable()
}
