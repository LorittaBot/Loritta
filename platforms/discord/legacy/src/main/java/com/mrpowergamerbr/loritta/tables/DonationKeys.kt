package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import org.jetbrains.exposed.dao.id.LongIdTable

object DonationKeys : LongIdTable() {
	val activeIn = optReference("active_in", ServerConfigs)
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
	val metadata = rawJsonb("metadata", gson).nullable()
}