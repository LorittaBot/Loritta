package net.perfectdreams.loritta.morenitta.tables

import net.perfectdreams.loritta.morenitta.utils.exposed.rawJsonb
import net.perfectdreams.loritta.morenitta.utils.gson
import org.jetbrains.exposed.dao.id.LongIdTable

object DonationKeys : LongIdTable() {
	val activeIn = optReference("active_in", ServerConfigs)
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
	val metadata = rawJsonb("metadata", gson).nullable()
}