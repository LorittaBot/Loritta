package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object BlacklistedGuilds : IdTable<Long>() {
	override val id: Column<EntityID<Long>> = long("guild").primaryKey().entityId()

	val bannedAt = long("banned_at")
	val reason = text("reason")
}