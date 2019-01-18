package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object DonationKeys : LongIdTable() {
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
}