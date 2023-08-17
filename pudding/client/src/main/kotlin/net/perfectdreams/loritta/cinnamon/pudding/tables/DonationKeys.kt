package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import org.jetbrains.exposed.dao.id.LongIdTable

object DonationKeys : LongIdTable() {
	val activeIn = optReference("active_in", ServerConfigs)
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
	val metadata = jsonb("metadata").nullable()
}