package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import org.jetbrains.exposed.dao.LongIdTable

object DonationKeys : LongIdTable() {
	val activeIn = optReference("active_in", ServerConfigs)
	val userId = long("user")
	val value = double("value")
	val expiresAt = long("expires_at")
	val metadata = rawJsonb("metadata", gson, jsonParser).nullable()
}