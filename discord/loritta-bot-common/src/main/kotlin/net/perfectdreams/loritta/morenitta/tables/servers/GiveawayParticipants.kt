package net.perfectdreams.loritta.morenitta.tables.servers

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.morenitta.utils.exposed.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object GiveawayParticipants : LongIdTable() {
	val giveawayId = long("giveaway_id").index()
	val userId = long("user_id").index()
	val joinedAt = timestampWithTimeZone("joined_at")
}