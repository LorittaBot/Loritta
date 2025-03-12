package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import org.jetbrains.exposed.dao.id.LongIdTable

object DonationKeys : LongIdTable() {
	val activeIn = optReference("active_in", ServerConfigs).index()
	val userId = long("user").index()
	val value = double("value")
	val expiresAt = long("expires_at")
	val metadata = jsonb("metadata").nullable()
}