package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.IdTable

object CachedTwitchChannels : IdTable<Long>() {
	override val id = long("twitch_user_id").entityId()
	val userLogin = text("user_login").uniqueIndex()
	val data = jsonb("data").nullable()
	val queriedAt = timestampWithTimeZone("queried_at")

	override val primaryKey = PrimaryKey(id)
}