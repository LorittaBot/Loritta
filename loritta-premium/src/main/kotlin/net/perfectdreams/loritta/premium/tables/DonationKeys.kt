package net.perfectdreams.loritta.premium.tables

import org.jetbrains.exposed.dao.LongIdTable

object DonationKeys : LongIdTable() {
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
}