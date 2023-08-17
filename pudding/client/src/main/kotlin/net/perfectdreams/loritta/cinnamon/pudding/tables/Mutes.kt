package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object Mutes : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val punishedById = long("punished_by")
	val content = text("content").nullable()
	val receivedAt = long("received_at")
	val isTemporary = bool("temporary").index()
	val expiresAt = long("expires_at").nullable()
	var userTimedOutUntil = timestampWithTimeZone("user_timed_out_until").nullable()
		.index()
}