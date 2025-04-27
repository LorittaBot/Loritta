package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object GiveawayParticipants : LongIdTable() {
	val giveawayId = long("giveaway_id").index()
	val userId = long("user_id").index()
	val joinedAt = timestampWithTimeZone("joined_at")
	val weight = integer("weight")
}