package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object GiveawayTemplates : LongIdTable() {
	val guildId = long("guild").index()
	val createdBy = long("created_by")
	val createdAt = timestampWithTimeZone("created_at")
	val lastUsedAt = timestampWithTimeZone("last_used_at").nullable()
	val name = text("name")
	val template = jsonb("template")
}